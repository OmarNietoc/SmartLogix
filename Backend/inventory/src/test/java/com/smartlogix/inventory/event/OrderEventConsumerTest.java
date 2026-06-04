package com.smartlogix.inventory.event;

import com.smartlogix.inventory.service.InventoryReservationService;
import com.smartlogix.inventory.event.ReservationConfirmedEvent;
import com.smartlogix.inventory.event.ReservationFailedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock private InventoryReservationService reservationService;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private OrderEventConsumer consumer;

    @Test
    @DisplayName("handleOrderCreated with empty items list skips reservation")
    void handleOrderCreated_emptyItems_skipsReservation() {
        OrderEvent event = buildEvent("order-1", List.of());

        consumer.handleOrderCreated(event);

        verifyNoInteractions(reservationService);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("handleOrderCreated with null items skips reservation")
    void handleOrderCreated_nullItems_skipsReservation() {
        OrderEvent event = buildEvent("order-1", null);

        consumer.handleOrderCreated(event);

        verifyNoInteractions(reservationService);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("handleOrderCreated with valid items reserves stock and publishes confirmed event")
    void handleOrderCreated_validItems_reservesStockAndPublishesConfirmed() {
        OrderItemEvent item = buildItem("prod-1", "wh-1", 2);
        OrderEvent event = buildEvent("order-1", List.of(item));

        consumer.handleOrderCreated(event);

        verify(reservationService).reserveStock(argThat(req ->
                req.getOrderId().equals("order-1") &&
                req.getProductId().equals("prod-1") &&
                req.getQuantity() == 2
        ));
        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.reservation.confirmed"), any(ReservationConfirmedEvent.class));
    }

    @Test
    @DisplayName("handleOrderCreated on stock failure compensates and publishes failed event")
    void handleOrderCreated_stockFailure_compensatesAndPublishesFailed() {
        OrderItemEvent item = buildItem("prod-1", "wh-1", 99);
        OrderEvent event = buildEvent("order-1", List.of(item));
        doThrow(new RuntimeException("stock insuficiente")).when(reservationService).reserveStock(any());

        consumer.handleOrderCreated(event);

        verify(reservationService).compensateAllForOrder("order-1");
        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.reservation.failed"), any(ReservationFailedEvent.class));
    }

    @Test
    @DisplayName("handleOrderCreated wraps unexpected exception as AMQP reject")
    void handleOrderCreated_unexpectedException_wrapsAsAmqpException() {
        OrderItemEvent item = buildItem("prod-1", "wh-1", 2);
        OrderEvent event = buildEvent("order-1", List.of(item));
        doThrow(new RuntimeException("DB down")).when(reservationService).reserveStock(any());
        doThrow(new RuntimeException("compensate also fails")).when(reservationService).compensateAllForOrder(any());

        assertThatThrownBy(() -> consumer.handleOrderCreated(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    private OrderEvent buildEvent(String orderId, List<OrderItemEvent> items) {
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setCustomerEmail("a@b.com");
        event.setCustomerName("Ana");
        event.setCompanyId("company-1");
        event.setShippingAddress("Calle 1");
        event.setItems(items);
        return event;
    }

    private OrderItemEvent buildItem(String productId, String warehouseId, int quantity) {
        OrderItemEvent item = new OrderItemEvent();
        item.setProductId(productId);
        item.setWarehouseId(warehouseId);
        item.setQuantity(quantity);
        return item;
    }
}
