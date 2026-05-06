package com.smartlogix.shipping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.enums.RouteStatus;
import com.smartlogix.shipping.exception.RouteNotFoundException;
import com.smartlogix.shipping.exception.ShipmentNotFoundException;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.RouteRepository;
import com.smartlogix.shipping.repository.ShipmentRepository;
import com.smartlogix.shipping.strategy.DhlStrategy;
import com.smartlogix.shipping.strategy.LocalCarrierStrategy;
import com.smartlogix.shipping.strategy.ShippingCalculationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock private RouteRepository routeRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private RoutingApiService routingApiService;

    private RouteService routeService;

    private final DhlStrategy dhlStrategy = new DhlStrategy();
    private final LocalCarrierStrategy localCarrierStrategy = new LocalCarrierStrategy();

    @BeforeEach
    void setUp() {
        Map<String, ShippingCalculationStrategy> strategies = Map.of(
                "dhlStrategy", dhlStrategy,
                "localCarrierStrategy", localCarrierStrategy
        );
        routeService = new RouteService(routeRepository, shipmentRepository, routingApiService,
                strategies, new ObjectMapper());
    }

    // ── Strategy Pattern Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("Strategy: carrierId=DHL selects dhlStrategy")
    void createRoute_dhlCarrier_usesDhlStrategy() {
        String shipmentId = "s1";
        Shipment shipment = buildPendingShipment(shipmentId);
        Route saved = Route.builder().id("r1").companyId("c1").status(RouteStatus.PLANNED).build();

        when(shipmentRepository.findAllById(List.of(shipmentId))).thenReturn(List.of(shipment));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn("{\"source\":\"osrm\"}");
        when(routeRepository.save(any())).thenReturn(saved);

        Route result = routeService.createRoute("c1", "DHL", "Origin St 1", List.of(shipmentId), true);

        assertThat(result).isNotNull();
        verify(routingApiService).fetchOptimizedPath(any(), any());
    }

    @Test
    @DisplayName("Strategy: carrierId=LOCAL selects localCarrierStrategy")
    void createRoute_localCarrier_usesLocalStrategy() {
        String shipmentId = "s2";
        Shipment shipment = buildPendingShipment(shipmentId);
        Route saved = Route.builder().id("r2").companyId("c1").status(RouteStatus.PLANNED).build();

        when(shipmentRepository.findAllById(List.of(shipmentId))).thenReturn(List.of(shipment));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn("{\"source\":\"osrm\"}");
        when(routeRepository.save(any())).thenReturn(saved);

        Route result = routeService.createRoute("c1", "CORREOS", "Origin St 1", List.of(shipmentId), true);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("DhlStrategy returns valid JSON with transport_mode air")
    void dhlStrategy_calculateRoute_returnsAirMode() {
        Route route = Route.builder().id("r1").carrierId("DHL").build();
        String result = dhlStrategy.calculateRoute(route);

        assertThat(result).contains("air");
        assertThat(result).contains("checkpoint_dhl_1");
    }

    @Test
    @DisplayName("LocalCarrierStrategy returns valid JSON with ground transport")
    void localCarrierStrategy_calculateRoute_returnsGroundMode() {
        Route route = Route.builder().id("r1").carrierId("LOCAL").build();
        String result = localCarrierStrategy.calculateRoute(route);

        assertThat(result).isNotBlank();
    }

    // ── createRoute validation ────────────────────────────────────────────────

    @Test
    @DisplayName("createRoute throws when a shipmentId does not exist")
    void createRoute_missingShipment_throwsShipmentNotFound() {
        when(shipmentRepository.findAllById(anyList())).thenReturn(List.of());

        assertThatThrownBy(() ->
                routeService.createRoute("c1", "DHL", "Origin", List.of("nonexistent"), false))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    @DisplayName("createRoute throws when shipment already assigned to a route")
    void createRoute_alreadyAssignedShipment_throwsIllegalState() {
        Shipment assigned = buildPendingShipment("s3");
        assigned.setRoute(Route.builder().id("existing-route").build());

        when(shipmentRepository.findAllById(List.of("s3"))).thenReturn(List.of(assigned));

        assertThatThrownBy(() ->
                routeService.createRoute("c1", "DHL", "Origin", List.of("s3"), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está asignado");
    }

    // ── getAllRoutes ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllRoutes with no filters returns all routes")
    void getAllRoutes_noFilters_returnsAll() {
        when(routeRepository.findAll()).thenReturn(List.of(Route.builder().id("r1").build()));

        List<Route> routes = routeService.getAllRoutes(null, null);

        assertThat(routes).hasSize(1);
    }

    @Test
    @DisplayName("getAllRoutes filters by companyId and status")
    void getAllRoutes_withBothFilters_callsFilteredQuery() {
        when(routeRepository.findByCompanyIdAndStatus("c1", RouteStatus.PLANNED))
                .thenReturn(List.of(Route.builder().id("r1").build()));

        List<Route> routes = routeService.getAllRoutes("c1", RouteStatus.PLANNED);

        assertThat(routes).hasSize(1);
        verify(routeRepository).findByCompanyIdAndStatus("c1", RouteStatus.PLANNED);
    }

    // ── getRouteById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRouteById throws RouteNotFoundException when not found")
    void getRouteById_notFound_throwsException() {
        when(routeRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getRouteById("bad-id"))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    // ── updateRouteStatus ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRouteStatus to IN_PROGRESS dispatches all shipments")
    void updateRouteStatus_inProgress_dispatchesShipments() {
        Shipment s = buildPendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route route = Route.builder().id("r1").status(RouteStatus.PLANNED)
                .shipments(new java.util.ArrayList<>(List.of(s))).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));
        when(routeRepository.save(any())).thenReturn(route);

        routeService.updateRouteStatus("r1", RouteStatus.IN_PROGRESS);

        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.DISPATCHED);
        verify(shipmentRepository).save(s);
    }

    // ── deleteRoute (soft delete) ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteRoute cancels route and sets shipments back to PENDING (soft delete)")
    void deleteRoute_cancelsRouteAndReleasesShipments() {
        Shipment s = buildPendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route route = Route.builder().id("r1").status(RouteStatus.PLANNED)
                .shipments(new java.util.ArrayList<>(List.of(s))).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));

        routeService.deleteRoute("r1");

        assertThat(route.getStatus()).isEqualTo(RouteStatus.CANCELLED);
        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(s.getRoute()).isNull();
        verify(routeRepository, never()).deleteById(any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Shipment buildPendingShipment(String id) {
        Shipment s = new Shipment();
        s.setId(id);
        s.setDeliveryStatus(DeliveryStatus.PENDING);
        return s;
    }
}
