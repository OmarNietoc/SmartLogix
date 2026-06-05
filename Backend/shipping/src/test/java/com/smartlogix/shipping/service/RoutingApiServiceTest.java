package com.smartlogix.shipping.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingApiServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RoutingApiService routingApiService = new RoutingApiService(objectMapper);

    @Test
    void fetchOptimizedPath_withoutShipmentCoordinates_returnsFallbackJson() throws Exception {
        Route route = Route.builder().originAddress("Centro de distribucion").build();
        List<Shipment> shipments = List.of(
                Shipment.builder().id("shipment-1").build(),
                Shipment.builder().id("shipment-2").build()
        );

        String result = routingApiService.fetchOptimizedPath(route, shipments);

        JsonNode json = objectMapper.readTree(result);
        assertThat(json.get("source").asText()).isEqualTo("fallback");
        assertThat(json.get("total_stops").asInt()).isEqualTo(2);
        assertThat(json.get("routed_stops").asInt()).isZero();
    }

    @Test
    void fallbackCalculateRoute_returnsFallbackJsonWithShipmentCount() throws Exception {
        Route route = Route.builder().originAddress("Centro de distribucion").build();
        List<Shipment> shipments = List.of(
                Shipment.builder().id("shipment-1").build(),
                Shipment.builder().id("shipment-2").build(),
                Shipment.builder().id("shipment-3").build()
        );

        String result = routingApiService.fallbackCalculateRoute(route, shipments, new RuntimeException("timeout"));

        JsonNode json = objectMapper.readTree(result);
        assertThat(json.get("source").asText()).isEqualTo("fallback");
        assertThat(json.get("total_stops").asInt()).isEqualTo(3);
        assertThat(json.get("routed_stops").asInt()).isZero();
    }
}
