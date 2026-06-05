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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartlogix.shipping.dto.RouteProposalRequestDTO;
import com.smartlogix.shipping.dto.RouteProposalResponseDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    private static final String COMPANY_ID = "c1";

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
        Route saved = Route.builder().id("r1").companyId(COMPANY_ID).status(RouteStatus.PLANNED).build();

        when(shipmentRepository.findAllById(List.of(shipmentId))).thenReturn(List.of(shipment));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn("{\"source\":\"osrm\"}");
        when(routeRepository.save(any())).thenReturn(saved);

        Route result = routeService.createRoute(COMPANY_ID, "DHL", "Origin St 1", List.of(shipmentId), true);

        assertThat(result).isNotNull();
        verify(routingApiService).fetchOptimizedPath(any(), any());
    }

    @Test
    @DisplayName("Strategy: carrierId non-DHL selects localCarrierStrategy")
    void createRoute_localCarrier_usesLocalStrategy() {
        String shipmentId = "s2";
        Shipment shipment = buildPendingShipment(shipmentId);
        Route saved = Route.builder().id("r2").companyId(COMPANY_ID).status(RouteStatus.PLANNED).build();

        when(shipmentRepository.findAllById(List.of(shipmentId))).thenReturn(List.of(shipment));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn("{\"source\":\"osrm\"}");
        when(routeRepository.save(any())).thenReturn(saved);

        Route result = routeService.createRoute(COMPANY_ID, "CORREOS", "Origin St 1", List.of(shipmentId), true);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("DhlStrategy returns JSON with transport_mode air")
    void dhlStrategy_calculateRoute_returnsAirMode() {
        Route route = Route.builder().id("r1").carrierId("DHL").build();
        String result = dhlStrategy.calculateRoute(route);

        assertThat(result).contains("air");
        assertThat(result).contains("checkpoint_dhl_1");
    }

    @Test
    @DisplayName("LocalCarrierStrategy returns JSON with transport_mode ground")
    void localCarrierStrategy_calculateRoute_returnsGroundMode() {
        Route route = Route.builder().id("r1").carrierId("LOCAL").build();
        String result = localCarrierStrategy.calculateRoute(route);

        assertThat(result).contains("ground");
        assertThat(result).contains("local_depot");
    }

    // ── createRoute validation ────────────────────────────────────────────────

    @Test
    @DisplayName("createRoute throws when shipmentId does not exist")
    void createRoute_missingShipment_throwsShipmentNotFound() {
        when(shipmentRepository.findAllById(anyList())).thenReturn(List.of());

        assertThatThrownBy(() ->
                routeService.createRoute(COMPANY_ID, "DHL", "Origin", List.of("nonexistent"), false))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    @DisplayName("createRoute throws when shipment already assigned to route")
    void createRoute_alreadyAssignedShipment_throwsIllegalState() {
        Shipment assigned = buildPendingShipment("s3");
        assigned.setRoute(Route.builder().id("existing-route").build());

        when(shipmentRepository.findAllById(List.of("s3"))).thenReturn(List.of(assigned));

        assertThatThrownBy(() ->
                routeService.createRoute(COMPANY_ID, "DHL", "Origin", List.of("s3"), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("s3");
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
        when(routeRepository.findByCompanyIdAndStatus(COMPANY_ID, RouteStatus.PLANNED))
                .thenReturn(List.of(Route.builder().id("r1").build()));

        List<Route> routes = routeService.getAllRoutes(COMPANY_ID, RouteStatus.PLANNED);

        assertThat(routes).hasSize(1);
        verify(routeRepository).findByCompanyIdAndStatus(COMPANY_ID, RouteStatus.PLANNED);
    }

    // ── getRouteById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRouteById throws RouteNotFoundException when not found")
    void getRouteById_notFound_throwsException() {
        when(routeRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getRouteById("bad-id", COMPANY_ID))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    // ── updateRouteStatus ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRouteStatus to IN_PROGRESS dispatches all shipments")
    void updateRouteStatus_inProgress_dispatchesShipments() {
        Shipment s = buildPendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route route = Route.builder().id("r1").companyId(COMPANY_ID).status(RouteStatus.PLANNED)
                .shipments(new ArrayList<>(List.of(s))).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));
        when(routeRepository.save(any())).thenReturn(route);

        routeService.updateRouteStatus("r1", RouteStatus.IN_PROGRESS, COMPANY_ID);

        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.DISPATCHED);
        verify(shipmentRepository).save(s);
    }

    // ── deleteRoute (soft delete) ─────────────────────────────────────────────

    @Test
    @DisplayName("deleteRoute cancels route and releases shipments to PENDING (soft delete)")
    void deleteRoute_cancelsRouteAndReleasesShipments() {
        Shipment s = buildPendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        Route route = Route.builder().id("r1").companyId(COMPANY_ID).status(RouteStatus.PLANNED)
                .shipments(new ArrayList<>(List.of(s))).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));

        routeService.deleteRoute("r1", COMPANY_ID);

        assertThat(route.getStatus()).isEqualTo(RouteStatus.CANCELLED);
        assertThat(s.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(s.getRoute()).isNull();
        verify(routeRepository, never()).deleteById(any());
    }

    // ── createRoute: optimizeRoute=false ─────────────────────────────────────

    @Test
    @DisplayName("createRoute with optimizeRoute=false uses manual JSON, skips routingApi")
    void createRoute_noOptimize_usesManualJson() {
        Shipment shipment = buildPendingShipment("s1");
        Route saved = Route.builder().id("r1").companyId(COMPANY_ID).build();

        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(shipment));
        when(routeRepository.save(any())).thenReturn(saved);

        Route result = routeService.createRoute(COMPANY_ID, "DHL", "Origin", List.of("s1"), false);

        assertThat(result).isNotNull();
        verify(routingApiService, never()).fetchOptimizedPath(any(), any());
    }

    @Test
    @DisplayName("createRoute throws when shipment status is not PENDING")
    void createRoute_nonPendingShipment_throwsIllegalState() {
        Shipment dispatched = buildPendingShipment("s1");
        dispatched.setDeliveryStatus(DeliveryStatus.DISPATCHED);

        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(dispatched));

        assertThatThrownBy(() ->
                routeService.createRoute(COMPANY_ID, "DHL", "Origin", List.of("s1"), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("s1");
    }

    // ── getAllRoutes filter variants ──────────────────────────────────────────

    @Test
    @DisplayName("getAllRoutes with companyId only calls company query")
    void getAllRoutes_companyIdOnly_callsCompanyQuery() {
        when(routeRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(Route.builder().id("r1").build()));

        List<Route> routes = routeService.getAllRoutes(COMPANY_ID, null);

        assertThat(routes).hasSize(1);
        verify(routeRepository).findByCompanyId(COMPANY_ID);
    }

    @Test
    @DisplayName("getAllRoutes with status only calls status query")
    void getAllRoutes_statusOnly_callsStatusQuery() {
        when(routeRepository.findByStatus(RouteStatus.PLANNED)).thenReturn(List.of(Route.builder().id("r1").build()));

        List<Route> routes = routeService.getAllRoutes(null, RouteStatus.PLANNED);

        assertThat(routes).hasSize(1);
        verify(routeRepository).findByStatus(RouteStatus.PLANNED);
    }

    // ── getRouteById wrong company ────────────────────────────────────────────

    @Test
    @DisplayName("getRouteById throws when route belongs to different company")
    void getRouteById_wrongCompany_throwsRouteNotFound() {
        Route route = Route.builder().id("r1").companyId("other-company").build();
        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> routeService.getRouteById("r1", COMPANY_ID))
                .isInstanceOf(RouteNotFoundException.class);
    }

    // ── updateRouteStatus non-IN_PROGRESS ────────────────────────────────────

    @Test
    @DisplayName("updateRouteStatus to COMPLETED does not dispatch shipments")
    void updateRouteStatus_toCompleted_noShipmentDispatching() {
        Shipment s = buildPendingShipment("s1");
        s.setDeliveryStatus(DeliveryStatus.DELIVERED);
        Route route = Route.builder().id("r1").companyId(COMPANY_ID).status(RouteStatus.IN_PROGRESS)
                .shipments(new ArrayList<>(List.of(s))).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));
        when(routeRepository.save(any())).thenReturn(route);

        routeService.updateRouteStatus("r1", RouteStatus.COMPLETED, COMPANY_ID);

        assertThat(route.getStatus()).isEqualTo(RouteStatus.COMPLETED);
        verify(shipmentRepository, never()).save(any());
    }

    // ── deleteRoute null shipments list ──────────────────────────────────────

    @Test
    @DisplayName("deleteRoute with null shipments list still cancels route")
    void deleteRoute_nullShipments_cancelsRoute() {
        Route route = Route.builder().id("r1").companyId(COMPANY_ID).status(RouteStatus.PLANNED)
                .shipments(null).build();

        when(routeRepository.findById("r1")).thenReturn(Optional.of(route));

        routeService.deleteRoute("r1", COMPANY_ID);

        assertThat(route.getStatus()).isEqualTo(RouteStatus.CANCELLED);
        verifyNoInteractions(shipmentRepository);
    }

    // ── generateProposal ─────────────────────────────────────────────────────

    @Test
    @DisplayName("generateProposal with coordinates already set returns proposal")
    void generateProposal_shipmentHasCoords_returnsProposal() {
        Shipment s1 = buildPendingShipment("s1");
        s1.setLatitude(BigDecimal.valueOf(-33.45));
        s1.setLongitude(BigDecimal.valueOf(-70.65));

        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("s1"));
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s1));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn(
                "{\"source\":\"osrm\",\"distance_km\":10.5,\"duration_minutes\":25,\"total_stops\":1,\"optimized_order\":[\"s1\"]}");

        RouteProposalResponseDTO result = routeService.generateProposal(request);

        assertThat(result.source()).isEqualTo("osrm");
        assertThat(result.distanceKm()).isEqualTo(10.5);
        assertThat(result.durationMinutes()).isEqualTo(25);
        assertThat(result.orderedShipments()).hasSize(1);
        assertThat(result.orderedShipments().get(0).shipmentId()).isEqualTo("s1");
        verify(routingApiService, never()).geocodeAddress(any());
    }

    @Test
    @DisplayName("generateProposal geocodes shipment without coordinates")
    void generateProposal_shipmentMissingCoords_geocodesAddress() {
        Shipment s1 = buildPendingShipment("s1");
        // no latitude/longitude set → null

        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("s1"));
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s1));
        when(routingApiService.geocodeAddress(s1.getShippingAddress())).thenReturn(new double[]{-33.45, -70.65});
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn(
                "{\"source\":\"geocoded\",\"total_stops\":1,\"optimized_order\":[\"s1\"]}");

        RouteProposalResponseDTO result = routeService.generateProposal(request);

        assertThat(s1.getLatitude()).isNotNull();
        assertThat(s1.getLongitude()).isNotNull();
        verify(routingApiService).geocodeAddress(s1.getShippingAddress());
        assertThat(result.source()).isEqualTo("geocoded");
    }

    @Test
    @DisplayName("generateProposal geocode failure is swallowed silently")
    void generateProposal_geocodeThrows_continuesWithoutCoords() {
        Shipment s1 = buildPendingShipment("s1");

        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("s1"));
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s1));
        when(routingApiService.geocodeAddress(any())).thenThrow(new RuntimeException("geo error"));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn(
                "{\"source\":\"fallback\",\"optimized_order\":[\"s1\"]}");

        assertThatCode(() -> routeService.generateProposal(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("generateProposal throws when no shipments found")
    void generateProposal_noShipments_throwsShipmentNotFound() {
        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("bad"));
        when(shipmentRepository.findAllById(List.of("bad"))).thenReturn(List.of());

        assertThatThrownBy(() -> routeService.generateProposal(request))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    @DisplayName("generateProposal returns fallback response when pathJson is invalid")
    void generateProposal_invalidPathJson_returnsFallback() {
        Shipment s1 = buildPendingShipment("s1");
        s1.setLatitude(BigDecimal.valueOf(-33.45));
        s1.setLongitude(BigDecimal.valueOf(-70.65));

        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("s1"));
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s1));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn("not-valid-json");

        RouteProposalResponseDTO result = routeService.generateProposal(request);

        assertThat(result.source()).isEqualTo("fallback");
        assertThat(result.orderedShipments()).hasSize(1);
    }

    @Test
    @DisplayName("generateProposal skips null IDs in optimized_order")
    void generateProposal_nullItemInOrder_skipped() {
        Shipment s1 = buildPendingShipment("s1");
        s1.setLatitude(BigDecimal.valueOf(-33.45));
        s1.setLongitude(BigDecimal.valueOf(-70.65));

        RouteProposalRequestDTO request = new RouteProposalRequestDTO("Origin 1", List.of("s1"));
        when(shipmentRepository.findAllById(List.of("s1"))).thenReturn(List.of(s1));
        when(routingApiService.fetchOptimizedPath(any(), any())).thenReturn(
                "{\"source\":\"test\",\"optimized_order\":[null,\"s1\"]}");

        RouteProposalResponseDTO result = routeService.generateProposal(request);

        assertThat(result.orderedShipments()).hasSize(1);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Shipment buildPendingShipment(String id) {
        Shipment s = new Shipment();
        s.setId(id);
        s.setCompanyId(COMPANY_ID);
        s.setDeliveryStatus(DeliveryStatus.PENDING);
        s.setShippingAddress("Calle Test 123");
        return s;
    }
}
