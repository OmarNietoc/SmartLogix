package com.smartlogix.shipping.mapper;

import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public ShipmentDTO toDto(Shipment shipment) {
        if (shipment == null) {
            return null;
        }

        return ShipmentDTO.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .routeId(shipment.getRoute() != null ? shipment.getRoute().getId() : null)
                .customerName(shipment.getCustomerName())
                .customerEmail(shipment.getCustomerEmail())
                .shippingAddress(shipment.getShippingAddress())
                .latitude(shipment.getLatitude())
                .longitude(shipment.getLongitude())
                .trackingNumber(shipment.getTrackingNumber())
                .deliveryStatus(shipment.getDeliveryStatus() != null ? shipment.getDeliveryStatus().name() : null)
                .estimatedDelivery(shipment.getEstimatedDelivery())
                .actualDelivery(shipment.getActualDelivery())
                .companyId(shipment.getCompanyId())
                .build();
    }

    public Shipment toEntity(ShipmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Shipment.builder()
                .id(dto.getId())
                .orderId(dto.getOrderId())
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .shippingAddress(dto.getShippingAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .trackingNumber(dto.getTrackingNumber())
                .deliveryStatus(parseDeliveryStatus(dto.getDeliveryStatus()))
                .estimatedDelivery(dto.getEstimatedDelivery())
                .actualDelivery(dto.getActualDelivery())
                .companyId(dto.getCompanyId())
                .build();
    }

    private DeliveryStatus parseDeliveryStatus(String deliveryStatus) {
        if (deliveryStatus == null || deliveryStatus.isBlank()) {
            return null;
        }
        return DeliveryStatus.valueOf(deliveryStatus);
    }
}
