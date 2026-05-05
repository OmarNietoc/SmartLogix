package com.smartlogix.shipping.event;

import com.smartlogix.shipping.config.RabbitMQConfig;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ShipmentRepository shipmentRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderCreated(OrderEvent event) {
        log.info("Creando envío para orden id={}", event.getOrderId());
        try {
            Shipment shipment = Shipment.builder()
                    .orderId(event.getOrderId())
                    .customerName(event.getCustomerName())
                    .customerEmail(event.getCustomerEmail())
                    .shippingAddress(event.getShippingAddress() != null ? event.getShippingAddress() : "Pendiente de asignación")
                    .trackingNumber("SL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .build();
            shipmentRepository.save(shipment);
            log.info("Envío creado con tracking={} para orden id={}", shipment.getTrackingNumber(), event.getOrderId());
        } catch (Exception e) {
            log.error("Error al crear envío para orden id={}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
