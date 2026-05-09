package com.smartlogix.shipping.service;

import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.event.OrderDeliveredEvent;
import com.smartlogix.shipping.event.OrderShippedEvent;
import com.smartlogix.shipping.event.ShippingEventPublisher;
import com.smartlogix.shipping.exception.ShipmentNotFoundException;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.RouteRepository;
import com.smartlogix.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final ShippingEventPublisher shippingEventPublisher;

    public List<Shipment> getAllShipments(String companyId, DeliveryStatus deliveryStatus) {
        if (deliveryStatus != null) {
            return shipmentRepository.findByCompanyIdAndDeliveryStatus(companyId, deliveryStatus);
        }
        return shipmentRepository.findByCompanyId(companyId);
    }

    public Shipment getShipmentByTrackingNumber(String trackingNumber, String companyId) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("El envío con número de seguimiento " + trackingNumber + " no fue encontrado."));
        if (!shipment.getCompanyId().equals(companyId)) {
            throw new ShipmentNotFoundException("El envío con número de seguimiento " + trackingNumber + " no fue encontrado.");
        }
        return shipment;
    }

    public Shipment getShipmentById(String id, String companyId) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("El envío con ID " + id + " no fue encontrado."));
        if (!shipment.getCompanyId().equals(companyId)) {
            throw new ShipmentNotFoundException("El envío con ID " + id + " no fue encontrado.");
        }
        return shipment;
    }

    @Transactional
    public Shipment createShipment(Shipment shipment) {
        if (shipment.getDeliveryStatus() == null) {
            shipment.setDeliveryStatus(DeliveryStatus.PENDING);
        }
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment updateShipmentStatus(String id, DeliveryStatus newStatus, String companyId) {
        Shipment existing = getShipmentById(id, companyId);
        if (!existing.getDeliveryStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException("Transición de estado no válida de " + existing.getDeliveryStatus() + " a " + newStatus);
        }
        existing.setDeliveryStatus(newStatus);
        if (newStatus == DeliveryStatus.DELIVERED) {
            existing.setActualDelivery(java.time.LocalDateTime.now());
        }
        Shipment saved = shipmentRepository.save(existing);

        if (newStatus == DeliveryStatus.DISPATCHED && saved.getCustomerEmail() != null) {
            shippingEventPublisher.publishOrderShipped(new OrderShippedEvent(
                    saved.getOrderId(),
                    saved.getCustomerEmail(),
                    saved.getTrackingNumber()
            ));
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            shippingEventPublisher.publishOrderDelivered(new OrderDeliveredEvent(
                    saved.getOrderId(),
                    saved.getTrackingNumber(),
                    saved.getCustomerEmail()
            ));
        }

        return saved;
    }

    @Transactional
    public void deleteShipment(String id, String companyId) {
        Shipment existing = getShipmentById(id, companyId);
        existing.setDeliveryStatus(DeliveryStatus.CANCELLED);
        shipmentRepository.save(existing);
    }
}
