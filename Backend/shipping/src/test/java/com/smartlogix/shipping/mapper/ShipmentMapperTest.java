package com.smartlogix.shipping.mapper;

import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ShipmentMapperTest {

    private final ShipmentMapper mapper = new ShipmentMapper();

    @Test
    @DisplayName("toDto maps all fields from Shipment to ShipmentDTO")
    void toDto_mapsAllFields() {
        Shipment shipment = Shipment.builder()
                .id("s1")
                .orderId("order-1")
                .customerName("Ana Pérez")
                .customerEmail("ana@test.com")
                .shippingAddress("Av. Principal 123")
                .trackingNumber("TRK-001")
                .deliveryStatus(DeliveryStatus.PENDING)
                .companyId("company-1")
                .build();

        ShipmentDTO dto = mapper.toDto(shipment);

        assertThat(dto.getId()).isEqualTo("s1");
        assertThat(dto.getOrderId()).isEqualTo("order-1");
        assertThat(dto.getCustomerName()).isEqualTo("Ana Pérez");
        assertThat(dto.getCustomerEmail()).isEqualTo("ana@test.com");
        assertThat(dto.getShippingAddress()).isEqualTo("Av. Principal 123");
        assertThat(dto.getTrackingNumber()).isEqualTo("TRK-001");
        assertThat(dto.getDeliveryStatus()).isEqualTo("PENDING");
        assertThat(dto.getCompanyId()).isEqualTo("company-1");
    }

    @Test
    @DisplayName("toDto handles null deliveryStatus")
    void toDto_nullDeliveryStatus_mapsToNull() {
        Shipment shipment = Shipment.builder()
                .id("s1")
                .deliveryStatus(null)
                .build();

        ShipmentDTO dto = mapper.toDto(shipment);

        assertThat(dto.getDeliveryStatus()).isNull();
    }

    @Test
    @DisplayName("toDto handles shipment with route")
    void toDto_withRoute_includesRouteId() {
        com.smartlogix.shipping.model.Route route = com.smartlogix.shipping.model.Route.builder()
                .id("route-1").build();
        Shipment shipment = Shipment.builder()
                .id("s1")
                .route(route)
                .build();

        ShipmentDTO dto = mapper.toDto(shipment);

        assertThat(dto.getRouteId()).isEqualTo("route-1");
    }

    @Test
    @DisplayName("toDto returns null when shipment is null")
    void toDto_null_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("toEntity maps all fields from ShipmentDTO to Shipment")
    void toEntity_mapsAllFields() {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId("s1");
        dto.setOrderId("order-1");
        dto.setCustomerName("Ana Pérez");
        dto.setCustomerEmail("ana@test.com");
        dto.setShippingAddress("Av. Principal 123");
        dto.setTrackingNumber("TRK-001");
        dto.setDeliveryStatus("ASSIGNED");
        dto.setCompanyId("company-1");

        Shipment shipment = mapper.toEntity(dto);

        assertThat(shipment.getId()).isEqualTo("s1");
        assertThat(shipment.getOrderId()).isEqualTo("order-1");
        assertThat(shipment.getDeliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(shipment.getCompanyId()).isEqualTo("company-1");
    }

    @Test
    @DisplayName("toEntity handles blank deliveryStatus as null")
    void toEntity_blankDeliveryStatus_mapsToNull() {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setDeliveryStatus("");

        Shipment shipment = mapper.toEntity(dto);

        assertThat(shipment.getDeliveryStatus()).isNull();
    }

    @Test
    @DisplayName("toEntity handles null deliveryStatus as null")
    void toEntity_nullDeliveryStatus_mapsToNull() {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setDeliveryStatus(null);

        Shipment shipment = mapper.toEntity(dto);

        assertThat(shipment.getDeliveryStatus()).isNull();
    }

    @Test
    @DisplayName("toEntity returns null when dto is null")
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
