package com.smartlogix.shipping.service;

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

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public Shipment getShipmentById(String id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("El envío con ID " + id + " no fue encontrado."));
    }

    @Transactional
    public Shipment createShipment(Shipment shipment) {
        // En caso de que se envíe asociado desde el json, el framework lo intenta poblar.
        // Se asume estado inicial "PENDING" si no se establece.
        if (shipment.getDeliveryStatus() == null) {
            shipment.setDeliveryStatus("PENDING");
        }
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment updateShipment(String id, Shipment updatedDetails) {
        Shipment existing = getShipmentById(id);

        if (updatedDetails.getCustomerName() != null) existing.setCustomerName(updatedDetails.getCustomerName());
        if (updatedDetails.getCustomerEmail() != null) existing.setCustomerEmail(updatedDetails.getCustomerEmail());
        if (updatedDetails.getShippingAddress() != null) existing.setShippingAddress(updatedDetails.getShippingAddress());
        if (updatedDetails.getLatitude() != null) existing.setLatitude(updatedDetails.getLatitude());
        if (updatedDetails.getLongitude() != null) existing.setLongitude(updatedDetails.getLongitude());
        if (updatedDetails.getDeliveryStatus() != null) existing.setDeliveryStatus(updatedDetails.getDeliveryStatus());
        if (updatedDetails.getTrackingNumber() != null) existing.setTrackingNumber(updatedDetails.getTrackingNumber());
        if (updatedDetails.getEstimatedDelivery() != null) existing.setEstimatedDelivery(updatedDetails.getEstimatedDelivery());
        if (updatedDetails.getActualDelivery() != null) existing.setActualDelivery(updatedDetails.getActualDelivery());

        return shipmentRepository.save(existing);
    }

    @Transactional
    public void deleteShipment(String id) {
        Shipment existing = getShipmentById(id);
        shipmentRepository.delete(existing);
    }
}
