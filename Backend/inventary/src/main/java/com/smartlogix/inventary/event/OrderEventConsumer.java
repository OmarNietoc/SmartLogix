package com.smartlogix.inventary.event;

import com.smartlogix.inventary.config.RabbitMQConfig;
import com.smartlogix.inventary.dto.StockReservationRequestDTO;
import com.smartlogix.inventary.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryReservationService reservationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderCreated(OrderEvent event) {
        log.info("Evento recibido: orderId={}, status={}", event.getOrderId(), event.getStatus());

        List<OrderItemEvent> items = event.getItems() != null ? event.getItems() : Collections.emptyList();

        if (items.isEmpty()) {
            log.warn("OrderEvent sin items para orderId={}. Reserva omitida.", event.getOrderId());
            return;
        }

        for (OrderItemEvent item : items) {
            StockReservationRequestDTO req = StockReservationRequestDTO.builder()
                    .orderId(event.getOrderId())
                    .productId(item.getProductId())
                    .warehouseId(item.getWarehouseId())
                    .quantity(item.getQuantity())
                    .build();
            try {
                reservationService.reserveStock(req);
                log.info("Stock reservado: orderId={}, productId={}, warehouseId={}, qty={}",
                        event.getOrderId(), item.getProductId(), item.getWarehouseId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Error reservando stock para orderId={}, productId={}: {}",
                        event.getOrderId(), item.getProductId(), e.getMessage());
            }
        }
    }
}
