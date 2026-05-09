package com.smartlogix.inventory.event;

import com.smartlogix.inventory.config.RabbitMQConfig;
import com.smartlogix.inventory.dto.StockReservationRequestDTO;
import com.smartlogix.inventory.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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

        try {
            for (OrderItemEvent item : items) {
                StockReservationRequestDTO req = StockReservationRequestDTO.builder()
                        .orderId(orderId)
                        .productId(item.getProductId())
                        .warehouseId(item.getWarehouseId())
                        .quantity(item.getQuantity())
                        .companyId(event.getCompanyId())
                        .build();
                try {
                    reservationService.reserveStock(req);
                    log.info("Stock reservado: orderId={}, productId={}, qty={}",
                            orderId, item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Fallo reserva orderId={}, productId={}: {}", orderId, item.getProductId(), e.getMessage());
                    reservationService.compensateAllForOrder(orderId);
                    try {
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.EXCHANGE_NAME,
                                "order.reservation.failed",
                                new ReservationFailedEvent(orderId, item.getProductId(), e.getMessage(),
                                        event.getCustomerEmail(), event.getCustomerName(), event.getCompanyId())
                        );
                    } catch (Exception mqEx) {
                        log.error("No se pudo publicar ReservationFailedEvent para orderId={}: {}", orderId, mqEx.getMessage());
                    }
                    return;
                }
            }

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "order.reservation.confirmed",
                    new ReservationConfirmedEvent(orderId, event.getCustomerEmail(), event.getCustomerName(), event.getShippingAddress(), event.getCompanyId())
            );
            log.info("Todos los items reservados para orderId={}. Publicando confirmed.", orderId);
        } catch (Exception e) {
            log.error("Error inesperado procesando order.created orderId={}: {}", orderId, e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Error procesando order.created para orderId=" + orderId, e);
        }
    }
}
