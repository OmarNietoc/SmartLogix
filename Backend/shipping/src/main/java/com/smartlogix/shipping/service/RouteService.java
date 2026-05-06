package com.smartlogix.shipping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipping.dto.OrderedShipmentDTO;
import com.smartlogix.shipping.dto.RouteProposalRequestDTO;
import com.smartlogix.shipping.dto.RouteProposalResponseDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.enums.RouteStatus;
import com.smartlogix.shipping.exception.RouteNotFoundException;
import com.smartlogix.shipping.exception.ShipmentNotFoundException;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.RouteRepository;
import com.smartlogix.shipping.repository.ShipmentRepository;
import com.smartlogix.shipping.strategy.ShippingCalculationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final RoutingApiService routingApiService;
    private final Map<String, ShippingCalculationStrategy> calculationStrategies;
    private final ObjectMapper objectMapper;

    @Transactional
    public Route createRoute(String companyId, String carrierId, String originAddress,
                             List<String> shipmentIds, boolean optimizeRoute) {
        log.info("Creando ruta para compañía: {}", companyId);

        Route route = Route.builder()
                .companyId(companyId)
                .carrierId(carrierId)
                .routeDate(LocalDate.now())
                .originAddress(originAddress)
                .status(RouteStatus.PLANNED)
                .build();

        List<Shipment> shipments = shipmentRepository.findAllById(shipmentIds);
        if (shipments.size() != shipmentIds.size()) {
            throw new ShipmentNotFoundException("Algunos IDs de envíos no existen o son inválidos.");
        }

        for (Shipment shipment : shipments) {
            if (shipment.getDeliveryStatus() != DeliveryStatus.PENDING || shipment.getRoute() != null) {
                throw new IllegalStateException(
                        "El envío con ID " + shipment.getId() + " ya está asignado a otra ruta o no está disponible.");
            }
            shipment.setRoute(route);
            shipment.setDeliveryStatus(DeliveryStatus.ASSIGNED);
            route.getShipments().add(shipment);
        }

        String strategyKey = determineStrategyKey(carrierId);
        ShippingCalculationStrategy strategy = calculationStrategies.get(strategyKey);
        if (strategy != null) {
            String routePlan = strategy.calculateRoute(route);
            log.info("Plan de ruta comercial generado: {}", routePlan);
        }

        String pathJson = optimizeRoute
                ? routingApiService.fetchOptimizedPath(route, shipments)
                : buildManualRouteJson(shipments);
        route.setOptimizedPathJson(pathJson);

        return routeRepository.save(route);
    }

    @Transactional(readOnly = true)
    public RouteProposalResponseDTO generateProposal(RouteProposalRequestDTO request) {
        List<Shipment> shipments = shipmentRepository.findAllById(request.shipmentIds());
        if (shipments.isEmpty()) {
            throw new ShipmentNotFoundException("No se encontraron envíos para los IDs proporcionados.");
        }

        for (Shipment s : shipments) {
            if (s.getLatitude() == null || s.getLongitude() == null) {
                try {
                    double[] coords = routingApiService.geocodeAddress(s.getShippingAddress());
                    s.setLatitude(BigDecimal.valueOf(coords[0]));
                    s.setLongitude(BigDecimal.valueOf(coords[1]));
                } catch (Exception e) {
                    log.warn("No se pudo geocodificar shipment {}: {}", s.getId(), e.getMessage());
                }
            }
        }

        Route tempRoute = Route.builder()
                .originAddress(request.originAddress())
                .build();

        String pathJson = routingApiService.fetchOptimizedPath(tempRoute, shipments);
        return buildProposalResponse(pathJson, shipments);
    }

    public List<Route> getAllRoutes(String companyId, RouteStatus status) {
        if (companyId != null && status != null) {
            return routeRepository.findByCompanyIdAndStatus(companyId, status);
        } else if (companyId != null) {
            return routeRepository.findByCompanyId(companyId);
        } else if (status != null) {
            return routeRepository.findByStatus(status);
        }
        return routeRepository.findAll();
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException("La ruta con ID " + id + " no fue encontrada."));
    }

    @Transactional
    public Route updateRouteStatus(String id, RouteStatus status) {
        Route existing = getRouteById(id);
        existing.setStatus(status);

        if (status == RouteStatus.IN_PROGRESS) {
            for (Shipment shipment : existing.getShipments()) {
                shipment.setDeliveryStatus(DeliveryStatus.DISPATCHED);
                shipmentRepository.save(shipment);
            }
        }

        return routeRepository.save(existing);
    }

    @Transactional
    public void deleteRoute(String id) {
        Route route = getRouteById(id);

        List<Shipment> shipments = route.getShipments();
        if (shipments != null) {
            for (Shipment shipment : shipments) {
                shipment.setRoute(null);
                shipment.setDeliveryStatus(DeliveryStatus.PENDING);
                shipmentRepository.save(shipment);
            }
            route.getShipments().clear();
        }

        route.setStatus(RouteStatus.CANCELLED);
        routeRepository.save(route);
    }

    private String buildManualRouteJson(List<Shipment> shipments) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("source", "manual");
            result.put("total_stops", shipments.size());
            result.put("optimized_order", shipments.stream().map(Shipment::getId).toList());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"source\":\"manual\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private RouteProposalResponseDTO buildProposalResponse(String pathJson, List<Shipment> shipments) {
        try {
            Map<String, Object> json = objectMapper.readValue(pathJson, Map.class);
            String source = (String) json.getOrDefault("source", "fallback");
            double distanceKm = ((Number) json.getOrDefault("distance_km", 0)).doubleValue();
            int durationMinutes = ((Number) json.getOrDefault("duration_minutes", 0)).intValue();
            int totalStops = ((Number) json.getOrDefault("total_stops", shipments.size())).intValue();

            List<String> optimizedOrder = (List<String>) json.getOrDefault("optimized_order",
                    shipments.stream().map(Shipment::getId).toList());

            Map<String, Shipment> shipmentMap = shipments.stream()
                    .collect(Collectors.toMap(Shipment::getId, s -> s));

            List<OrderedShipmentDTO> ordered = new ArrayList<>();
            for (int i = 0; i < optimizedOrder.size(); i++) {
                String sid = optimizedOrder.get(i);
                if (sid == null) continue;
                Shipment s = shipmentMap.get(sid);
                if (s != null) {
                    ordered.add(new OrderedShipmentDTO(s.getId(), s.getShippingAddress(),
                            s.getLatitude(), s.getLongitude(), i + 1));
                }
            }

            return new RouteProposalResponseDTO(source, distanceKm, durationMinutes, totalStops, ordered);
        } catch (Exception e) {
            log.error("Error parseando pathJson para propuesta: {}", e.getMessage());
            List<OrderedShipmentDTO> ordered = new ArrayList<>();
            for (int i = 0; i < shipments.size(); i++) {
                Shipment s = shipments.get(i);
                ordered.add(new OrderedShipmentDTO(s.getId(), s.getShippingAddress(),
                        s.getLatitude(), s.getLongitude(), i + 1));
            }
            return new RouteProposalResponseDTO("fallback", 0, 0, shipments.size(), ordered);
        }
    }

    private String determineStrategyKey(String carrierId) {
        if ("DHL".equalsIgnoreCase(carrierId)) {
            return "dhlStrategy";
        }
        return "localCarrierStrategy";
    }
}
