package com.smartlogix.shipping.mapper;

import com.smartlogix.shipping.dto.RouteDTO;
import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.enums.RouteStatus;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingMapperTest {

    private final ShipmentMapper shipmentMapper = new ShipmentMapper();

    @Test
    void shipmentToDto_mapsRouteIdStatusAndDeliveryFields() {
        LocalDateTime estimated = LocalDateTime.of(2026, 6, 5, 10, 30);
        LocalDateTime actual = LocalDateTime.of(2026, 6, 6, 11, 15);
        Route route = Route.builder().id("route-1").build();
        Shipment shipment = Shipment.builder()
                .id("shipment-1")
                .orderId("order-1")
                .route(route)
                .customerName("Cliente Demo")
                .customerEmail("cliente@smartlogix.cl")
                .shippingAddress("Av. Demo 123")
                .latitude(new BigDecimal("-33.4489000"))
                .longitude(new BigDecimal("-70.6693000"))
                .trackingNumber("SL-ABC12345")
                .deliveryStatus(DeliveryStatus.DISPATCHED)
                .estimatedDelivery(estimated)
                .actualDelivery(actual)
                .companyId("company-1")
                .build();

        ShipmentDTO dto = shipmentMapper.toDto(shipment);

        assertThat(dto.getId()).isEqualTo("shipment-1");
        assertThat(dto.getRouteId()).isEqualTo("route-1");
        assertThat(dto.getDeliveryStatus()).isEqualTo("DISPATCHED");
        assertThat(dto.getEstimatedDelivery()).isEqualTo(estimated);
        assertThat(dto.getActualDelivery()).isEqualTo(actual);
        assertThat(dto.getCompanyId()).isEqualTo("company-1");
    }

    @Test
    void shipmentToEntity_parsesDeliveryStatusAndKeepsFlatFields() {
        ShipmentDTO dto = ShipmentDTO.builder()
                .id("shipment-1")
                .orderId("order-1")
                .customerName("Cliente Demo")
                .customerEmail("cliente@smartlogix.cl")
                .shippingAddress("Av. Demo 123")
                .latitude(new BigDecimal("-33.4489000"))
                .longitude(new BigDecimal("-70.6693000"))
                .trackingNumber("SL-ABC12345")
                .deliveryStatus("DELIVERED")
                .companyId("company-1")
                .build();

        Shipment entity = shipmentMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo("shipment-1");
        assertThat(entity.getRoute()).isNull();
        assertThat(entity.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(entity.getShippingAddress()).isEqualTo("Av. Demo 123");
    }

    @Test
    void shipmentMapper_handlesNullBlankAndInvalidStatus() {
        assertThat(shipmentMapper.toDto(null)).isNull();
        assertThat(shipmentMapper.toEntity(null)).isNull();

        Shipment blankStatus = shipmentMapper.toEntity(ShipmentDTO.builder().deliveryStatus(" ").build());
        assertThat(blankStatus.getDeliveryStatus()).isNull();

        assertThatThrownBy(() -> shipmentMapper.toEntity(ShipmentDTO.builder().deliveryStatus("UNKNOWN").build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void routeMapper_mapsShipmentsAndIgnoresShipmentsWhenCreatingEntity() {
        RouteMapperImpl routeMapper = new RouteMapperImpl();
        ReflectionTestUtils.setField(routeMapper, "shipmentMapper", shipmentMapper);

        Shipment shipment = Shipment.builder()
                .id("shipment-1")
                .orderId("order-1")
                .deliveryStatus(DeliveryStatus.PENDING)
                .companyId("company-1")
                .build();
        Route route = Route.builder()
                .id("route-1")
                .companyId("company-1")
                .carrierId("DHL")
                .routeDate(LocalDate.of(2026, 6, 4))
                .originAddress("Centro de distribucion")
                .optimizedPathJson("{\"source\":\"fallback\"}")
                .status(RouteStatus.PLANNED)
                .shipments(new ArrayList<>(List.of(shipment)))
                .build();
        shipment.setRoute(route);

        RouteDTO dto = routeMapper.toDto(route);

        assertThat(dto.getId()).isEqualTo("route-1");
        assertThat(dto.getStatus()).isEqualTo("PLANNED");
        assertThat(dto.getShipments()).hasSize(1);
        assertThat(dto.getShipments().get(0).getRouteId()).isEqualTo("route-1");

        Route entity = routeMapper.toEntity(dto);
        assertThat(entity.getStatus()).isEqualTo(RouteStatus.PLANNED);
        assertThat(entity.getShipments()).isEmpty();
    }
}
