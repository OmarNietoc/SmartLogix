package com.smartlogix.shipping.event;

import com.smartlogix.shipping.config.RabbitMQConfig;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationConfirmedConsumer {

    private final ShipmentRepository shipmentRepository;

    @RabbitListener(queues = RabbitMQConfig.CONFIRMED_QUEUE_NAME)
    public void handleReservationConfirmed(ReservationConfirmedEvent event) {
        log.info("Creando envío para orden confirmada id={}", event.orderId());
        try {
            Shipment shipment = Shipment.builder()
                    .orderId(event.orderId())
                    .customerName(event.customerName())
                    .customerEmail(event.customerEmail())
                    .shippingAddress(event.shippingAddress() != null ? event.shippingAddress() : "Pendiente de asignación")
                    .trackingNumber("SL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .build();
            shipmentRepository.save(shipment);
            log.info("Envío creado tracking={} para orderId={}", shipment.getTrackingNumber(), event.orderId());
        } catch (Exception e) {
            log.error("Error creando envío para orderId={}: {}", event.orderId(), e.getMessage());
        }
    }
}
