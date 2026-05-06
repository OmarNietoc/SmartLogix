package com.smartlogix.order.service;

import com.smartlogix.order.dto.*;
import com.smartlogix.order.exception.ResourceNotFoundException;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private OrderService orderService;

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder saves order and publishes RabbitMQ event")
    void createOrder_happyPath_savesAndPublishesEvent() {
        CreateOrderRequest request = buildCreateRequest("Juan", "juan@email.com", 2, BigDecimal.valueOf(1000));
        Order savedOrder = buildSavedOrder("order-1", "Juan", "juan@email.com", 2, BigDecimal.valueOf(1000));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.id()).isEqualTo("order-1");
        assertThat(response.customerName()).isEqualTo("Juan");
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.created"), any(Object.class));
    }

    @Test
    @DisplayName("createOrder builds full shipping address from parts")
    void createOrder_buildsShippingAddressFromParts() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ana", "ana@email.com",
                "Calle 1", "Providencia", "Santiago", "RM",
                List.of(new OrderItemRequest("p1", "w1", "Producto", 1, BigDecimal.TEN))
        );
        Order saved = buildSavedOrder("o2", "Ana", "ana@email.com", 1, BigDecimal.TEN);
        when(orderRepository.save(any())).thenReturn(saved);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        orderService.createOrder(request);
        verify(orderRepository).save(captor.capture());

        assertThat(captor.getValue().getShippingAddress()).contains("Calle 1", "Providencia", "Chile");
    }

    @Test
    @DisplayName("createOrder calculates total as sum of price * quantity per item")
    void createOrder_calculatesTotal() {
        OrderItemRequest item1 = new OrderItemRequest("p1", "w1", "A", 3, BigDecimal.valueOf(100));
        OrderItemRequest item2 = new OrderItemRequest("p2", "w1", "B", 2, BigDecimal.valueOf(50));
        CreateOrderRequest request = new CreateOrderRequest(
                "Carlos", "c@mail.com", "St", "Comm", "City", "Reg", List.of(item1, item2));

        Order saved = buildSavedOrder("o3", "Carlos", "c@mail.com", 5, BigDecimal.valueOf(400));
        when(orderRepository.save(any())).thenReturn(saved);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        orderService.createOrder(request);
        verify(orderRepository).save(captor.capture());

        assertThat(captor.getValue().getTotal()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    // ── getAllOrders ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders returns mapped list")
    void getAllOrders_returnsAllMapped() {
        when(orderRepository.findAll()).thenReturn(List.of(
                buildSavedOrder("o1", "A", "a@a.com", 1, BigDecimal.TEN),
                buildSavedOrder("o2", "B", "b@b.com", 1, BigDecimal.ONE)
        ));

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("o1");
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById throws ResourceNotFoundException when not found")
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById("bad"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("bad");
    }

    @Test
    @DisplayName("getOrderById returns mapped response when found")
    void getOrderById_found_returnsResponse() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById("o1");

        assertThat(response.id()).isEqualTo("o1");
        assertThat(response.customerEmail()).isEqualTo("j@j.com");
    }

    // ── updateOrderStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus PENDING→CONFIRMED succeeds and publishes event")
    void updateOrderStatus_validTransition_savesAndPublishes() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.updateOrderStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.updated"), any(Object.class));
    }

    @Test
    @DisplayName("updateOrderStatus throws on invalid transition DELIVERED→CONFIRMED")
    void updateOrderStatus_invalidTransition_throwsIllegalState() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateOrderStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transición inválida");
    }

    // ── OrderStatus state machine ─────────────────────────────────────────────

    @Test
    @DisplayName("OrderStatus state machine: PENDING allows CONFIRMED, REJECTED, CANCELLED")
    void orderStatus_pendingTransitions() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.REJECTED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    @DisplayName("OrderStatus state machine: DELIVERED is terminal")
    void orderStatus_deliveredIsTerminal() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    @DisplayName("OrderStatus state machine: CONFIRMED→SHIPPED is valid")
    void orderStatus_confirmedToShipped() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateOrderRequest buildCreateRequest(String name, String email, int qty, BigDecimal price) {
        return new CreateOrderRequest(name, email, "Calle 1", "Comm", "City", "Reg",
                List.of(new OrderItemRequest("p1", "w1", "Producto", qty, price)));
    }

    private Order buildSavedOrder(String id, String name, String email, int qty, BigDecimal pricePerUnit) {
        OrderItem item = new OrderItem();
        item.setId("item-" + id);
        item.setProductId("p1");
        item.setWarehouseId("w1");
        item.setProductName("Producto");
        item.setQuantity(qty);
        item.setPrice(pricePerUnit);

        Order order = new Order();
        order.setId(id);
        order.setCustomerName(name);
        order.setCustomerEmail(email);
        order.setShippingAddress("Calle 1, Comm, City, Reg, Chile");
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(pricePerUnit.multiply(BigDecimal.valueOf(qty)));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>(List.of(item)));
        item.setOrder(order);
        return order;
    }
}
