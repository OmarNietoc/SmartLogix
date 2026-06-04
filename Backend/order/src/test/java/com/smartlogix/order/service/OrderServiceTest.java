package com.smartlogix.order.service;

import com.smartlogix.order.dto.*;
import com.smartlogix.order.exception.ResourceNotFoundException;
import com.smartlogix.order.mapper.OrderMapper;
import com.smartlogix.order.model.*;
import com.smartlogix.order.model.Region;
import com.smartlogix.order.repository.ComunaRepository;
import com.smartlogix.order.repository.OrderRepository;
import com.smartlogix.order.repository.RegionRepository;
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
    private static final String COMPANY_ID = "c1";

    @Mock private OrderRepository orderRepository;
    @Mock private ComunaRepository comunaRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderService orderService;

    // ── createOrder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder saves order and publishes RabbitMQ event")
    void createOrder_happyPath_savesAndPublishesEvent() {
        Comuna comuna = buildDefaultComuna();
        CreateOrderRequest request = buildCreateRequest("Juan", "juan@email.com", 2, BigDecimal.valueOf(1000));
        Order savedOrder = buildSavedOrder("order-1", "Juan", "juan@email.com", 2, BigDecimal.valueOf(1000));
        OrderResponse expected = buildOrderResponse("order-1", "Juan", "juan@email.com",
                BigDecimal.valueOf(2000), OrderStatus.PENDING);

        when(comunaRepository.findById(13123)).thenReturn(Optional.of(comuna));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(expected);

        OrderResponse response = orderService.createOrder(request, COMPANY_ID);

        assertThat(response.id()).isEqualTo("order-1");
        assertThat(response.customerName()).isEqualTo("Juan");
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.created"), any(Object.class));
        verify(orderMapper).toDto(savedOrder);
    }

    @Test
    @DisplayName("createOrder sets street and commune on order")
    void createOrder_setsStreetAndCommune() {
        Comuna comuna = buildDefaultComuna();
        CreateOrderRequest request = new CreateOrderRequest(
                "Ana", "ana@email.com", "Calle 1", 13123,
                List.of(new OrderItemRequest("p1", "w1", "Producto", 1, BigDecimal.TEN)), COMPANY_ID
        );
        Order saved = buildSavedOrder("o2", "Ana", "ana@email.com", 1, BigDecimal.TEN);

        when(comunaRepository.findById(13123)).thenReturn(Optional.of(comuna));
        when(orderRepository.save(any())).thenReturn(saved);
        when(orderMapper.toDto(any(Order.class))).thenReturn(
                buildOrderResponse("o2", "Ana", "ana@email.com", BigDecimal.TEN, OrderStatus.PENDING));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        orderService.createOrder(request, COMPANY_ID);
        verify(orderRepository).save(captor.capture());

        assertThat(captor.getValue().getStreet()).isEqualTo("Calle 1");
        assertThat(captor.getValue().getComuna().getNombre()).isEqualTo("Providencia");
    }

    @Test
    @DisplayName("createOrder calculates total as sum of price * quantity per item")
    void createOrder_calculatesTotal() {
        OrderItemRequest item1 = new OrderItemRequest("p1", "w1", "A", 3, BigDecimal.valueOf(100));
        OrderItemRequest item2 = new OrderItemRequest("p2", "w1", "B", 2, BigDecimal.valueOf(50));
        CreateOrderRequest request = new CreateOrderRequest(
                "Carlos", "c@mail.com", "St", 13123, List.of(item1, item2), COMPANY_ID);

        Order saved = buildSavedOrder("o3", "Carlos", "c@mail.com", 5, BigDecimal.valueOf(400));

        when(comunaRepository.findById(13123)).thenReturn(Optional.of(buildDefaultComuna()));
        when(orderRepository.save(any())).thenReturn(saved);
        when(orderMapper.toDto(any(Order.class))).thenReturn(
                buildOrderResponse("o3", "Carlos", "c@mail.com", BigDecimal.valueOf(400), OrderStatus.PENDING));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        orderService.createOrder(request, COMPANY_ID);
        verify(orderRepository).save(captor.capture());

        assertThat(captor.getValue().getTotal()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }

    @Test
    @DisplayName("createOrder throws ResourceNotFoundException when comunaId not found")
    void createOrder_invalidComunaId_throwsException() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Test", "t@t.com", "Calle", 99999,
                List.of(new OrderItemRequest("p1", "w1", "P", 1, BigDecimal.TEN)), COMPANY_ID);

        when(comunaRepository.findById(99999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request, COMPANY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99999");
    }

    // ── getAllOrders ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllOrders returns mapped list")
    void getAllOrders_returnsAllMapped() {
        Order o1 = buildSavedOrder("o1", "A", "a@a.com", 1, BigDecimal.TEN);
        Order o2 = buildSavedOrder("o2", "B", "b@b.com", 1, BigDecimal.ONE);
        when(orderRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(o1, o2));
        when(orderMapper.toDto(o1)).thenReturn(
                buildOrderResponse("o1", "A", "a@a.com", BigDecimal.TEN, OrderStatus.PENDING));
        when(orderMapper.toDto(o2)).thenReturn(
                buildOrderResponse("o2", "B", "b@b.com", BigDecimal.ONE, OrderStatus.PENDING));

        List<OrderResponse> result = orderService.getAllOrders(COMPANY_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("o1");
    }

    // ── getOrderById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById throws ResourceNotFoundException when not found")
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById("bad", COMPANY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("bad");
    }

    @Test
    @DisplayName("getOrderById returns mapped response when found")
    void getOrderById_found_returnsResponse() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(
                buildOrderResponse("o1", "Juan", "j@j.com", BigDecimal.TEN, OrderStatus.PENDING));

        OrderResponse response = orderService.getOrderById("o1", COMPANY_ID);

        assertThat(response.id()).isEqualTo("o1");
        assertThat(response.customerEmail()).isEqualTo("j@j.com");
    }

    // ── updateOrderStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus PENDING->CONFIRMED succeeds and publishes event")
    void updateOrderStatus_validTransition_savesAndPublishes() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(
                buildOrderResponse("o1", "Juan", "j@j.com", BigDecimal.TEN, OrderStatus.CONFIRMED));

        orderService.updateOrderStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED), COMPANY_ID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.updated"), any(Object.class));
    }

    @Test
    @DisplayName("updateOrderStatus throws on invalid transition DELIVERED->CONFIRMED")
    void updateOrderStatus_invalidTransition_throwsIllegalState() {
        Order order = buildSavedOrder("o1", "Juan", "j@j.com", 1, BigDecimal.TEN);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateOrderStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED), COMPANY_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED");
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
    @DisplayName("OrderStatus state machine: CONFIRMED->SHIPPED is valid")
    void orderStatus_confirmedToShipped() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }

    // ── getOrderById wrong company ────────────────────────────────────────────

    @Test
    @DisplayName("getOrderById throws when order belongs to different company")
    void getOrderById_wrongCompany_throwsException() {
        Order order = buildSavedOrder("o1", "A", "a@a.com", 1, BigDecimal.TEN);
        order.setCompanyId("other-company");
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById("o1", COMPANY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateOrderStatus wrong company ───────────────────────────────────────

    @Test
    @DisplayName("updateOrderStatus throws when order belongs to different company")
    void updateOrderStatus_wrongCompany_throwsException() {
        Order order = buildSavedOrder("o1", "A", "a@a.com", 1, BigDecimal.TEN);
        order.setCompanyId("other-company");
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateOrderStatus("o1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED), COMPANY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAllRegiones ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllRegiones returns mapped list of regions")
    void getAllRegiones_returnsMapped() {
        Pais pais = Pais.builder().id(1).nombre("Chile").build();
        Region region = Region.builder().id(13).nombre("Región Metropolitana").pais(pais).build();
        when(regionRepository.findAll()).thenReturn(List.of(region));

        List<RegionResponse> result = orderService.getAllRegiones();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Región Metropolitana");
    }

    // ── getComunasByRegion ────────────────────────────────────────────────────

    @Test
    @DisplayName("getComunasByRegion returns communes for given region")
    void getComunasByRegion_returnsMapped() {
        when(comunaRepository.findByRegionId(13)).thenReturn(List.of(buildDefaultComuna()));

        List<ComunaResponse> result = orderService.getComunasByRegion(13);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Providencia");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CreateOrderRequest buildCreateRequest(String name, String email, int qty, BigDecimal price) {
        return new CreateOrderRequest(name, email, "Calle 1", 13123,
                List.of(new OrderItemRequest("p1", "w1", "Producto", qty, price)), COMPANY_ID);
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
        order.setCompanyId(COMPANY_ID);
        order.setStreet("Calle 1");
        order.setComuna(buildDefaultComuna());
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(pricePerUnit.multiply(BigDecimal.valueOf(qty)));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>(List.of(item)));
        item.setOrder(order);
        return order;
    }

    private OrderResponse buildOrderResponse(String id, String name, String email,
                                              BigDecimal total, OrderStatus status) {
        return new OrderResponse(id, name, email, "Calle 1", 13123,
                "Providencia", "Región Metropolitana de Santiago",
                status, total, LocalDateTime.now(), LocalDateTime.now(), List.of(), COMPANY_ID);
    }

    private Comuna buildDefaultComuna() {
        Pais pais = Pais.builder().id(1).nombre("Chile").build();
        Region region = Region.builder().id(13).nombre("Región Metropolitana de Santiago").pais(pais).build();
        return Comuna.builder().id(13123).nombre("Providencia").region(region).build();
    }
}
