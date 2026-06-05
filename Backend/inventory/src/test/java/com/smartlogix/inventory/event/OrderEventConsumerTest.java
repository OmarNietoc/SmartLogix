package com.smartlogix.inventory.event;

import com.smartlogix.inventory.config.RabbitMQConfig;
import com.smartlogix.inventory.dto.StockReservationRequestDTO;
import com.smartlogix.inventory.service.InventoryReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock private InventoryReservationService reservationService;
    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private OrderEventConsumer consumer;

    @Test
    void handleOrderCreated_withNoItems_doesNotReserveOrPublish() {
        consumer.handleOrderCreated(orderEvent(List.of()));

        verify(reservationService, never()).reserveStock(any());
        verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(Object.class));
    }

    @Test
    void handleOrderCreated_reservesEveryItemAndPublishesConfirmed() {
        consumer.handleOrderCreated(orderEvent(List.of(item("product-1"), item("product-2"))));

        ArgumentCaptor<StockReservationRequestDTO> requestCaptor = ArgumentCaptor.forClass(StockReservationRequestDTO.class);
        verify(reservationService, times(2)).reserveStock(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues()).extracting(StockReservationRequestDTO::getProductId)
                .containsExactly("product-1", "product-2");
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("order.reservation.confirmed"),
                any(ReservationConfirmedEvent.class)
        );
    }

    @Test
    void handleOrderCreated_whenReservationFails_compensatesAndPublishesFailure() {
        when(reservationService.reserveStock(any())).thenThrow(new IllegalStateException("Stock insuficiente"));

        consumer.handleOrderCreated(orderEvent(List.of(item("product-1"))));

        verify(reservationService).compensateAllForOrder("order-1");
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq("order.reservation.failed"),
                any(ReservationFailedEvent.class)
        );
    }

    private OrderEvent orderEvent(List<OrderItemEvent> items) {
        return OrderEvent.builder()
                .orderId("order-1")
                .customerEmail("cliente@smartlogix.cl")
                .customerName("Cliente")
                .shippingAddress("Av. Demo 123")
                .companyId("company-1")
                .items(items)
                .build();
    }

    private OrderItemEvent item(String productId) {
        return OrderItemEvent.builder()
                .productId(productId)
                .warehouseId("warehouse-1")
                .quantity(2)
                .build();
    }
}
