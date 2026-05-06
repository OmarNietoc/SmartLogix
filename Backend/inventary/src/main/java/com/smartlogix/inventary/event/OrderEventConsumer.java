package com.smartlogix.inventary.event;

import com.smartlogix.inventary.config.RabbitMQConfig;
import com.smartlogix.inventary.dto.StockReservationRequestDTO;
import com.smartlogix.inventary.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryReservationService reservationService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderCreated(OrderEvent event) {
        String orderId = event.getOrderId();
        log.info("Procesando order.created: orderId={}", orderId);

        List<OrderItemEvent> items = event.getItems() != null ? event.getItems() : Collections.emptyList();
        if (items.isEmpty()) {
            log.warn("OrderEvent sin items para orderId={}. Reserva omitida.", orderId);
            return;
        }

        for (OrderItemEvent item : items) {
            StockReservationRequestDTO req = StockReservationRequestDTO.builder()
                    .orderId(orderId)
                    .productId(item.getProductId())
                    .warehouseId(item.getWarehouseId())
                    .quantity(item.getQuantity())
                    .build();
            try {
                reservationService.reserveStock(req);
                log.info("Stock reservado: orderId={}, productId={}, qty={}",
                        orderId, item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Fallo reserva orderId={}, productId={}: {}", orderId, item.getProductId(), e.getMessage());
                reservationService.compensateAllForOrder(orderId);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        "order.reservation.failed",
                        new ReservationFailedEvent(orderId, item.getProductId(), e.getMessage(),
                                event.getCustomerEmail(), event.getCustomerName())
                );
                return;
            }
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                "order.reservation.confirmed",
                new ReservationConfirmedEvent(orderId, event.getCustomerEmail(), event.getCustomerName())
        );
        log.info("Todos los items reservados para orderId={}. Publicando confirmed.", orderId);
    }
}
