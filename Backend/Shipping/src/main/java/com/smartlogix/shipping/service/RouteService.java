package com.smartlogix.shipping.service;

import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.RouteRepository;
import com.smartlogix.shipping.repository.ShipmentRepository;
import com.smartlogix.shipping.strategy.ShippingCalculationStrategy;
import com.smartlogix.shipping.exception.RouteNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final RoutingApiService routingApiService;
    private final Map<String, ShippingCalculationStrategy> calculationStrategies;

    @Transactional
    public Route createRoute(String companyId, String carrierId, String originAddress, List<Shipment> shipments) {
        log.info("Creando ruta para la compañía: {}", companyId);
        
        Route route = Route.builder()
                .companyId(companyId)
                .carrierId(carrierId)
                .routeDate(LocalDate.now())
                .originAddress(originAddress)
                .status("CREATED")
                .build();
        
        // Asociar pedidos a la ruta
        for (Shipment shipment : shipments) {
            shipment.setRoute(route);
            route.getShipments().add(shipment);
        }

        // Determinar qué estrategia usar (DhlStrategy, LocalCarrierStrategy, etc. basado en carrierId)
        String strategyKey = determineStrategyKey(carrierId);
        ShippingCalculationStrategy strategy = calculationStrategies.get(strategyKey);

        if (strategy != null) {
            String routePlan = strategy.calculateRoute(route);
            log.info("Plan de ruta comercial generado: {}", routePlan);
        }

        // Llamar a la API externa para el path geográfico, protegido con CircuitBreaker
        String pathJson = routingApiService.fetchOptimizedPath(route);
        route.setOptimizedPathJson(pathJson);
        
        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException("La ruta con ID " + id + " no fue encontrada."));
    }

    @Transactional
    public Route updateRoute(String id, Route updatedDetails) {
        Route existing = getRouteById(id);
        if (updatedDetails.getStatus() != null) existing.setStatus(updatedDetails.getStatus());
        if (updatedDetails.getCarrierId() != null) existing.setCarrierId(updatedDetails.getCarrierId());
        if (updatedDetails.getRouteDate() != null) existing.setRouteDate(updatedDetails.getRouteDate());
        if (updatedDetails.getOriginAddress() != null) existing.setOriginAddress(updatedDetails.getOriginAddress());

        return routeRepository.save(existing);
    }

    @Transactional
    public void deleteRoute(String id) {
        Route route = getRouteById(id);
        
        // Orphan Management (Desasignación)
        List<Shipment> shipments = route.getShipments();
        if (shipments != null) {
            for (Shipment shipment : shipments) {
                shipment.setRoute(null);
                shipment.setDeliveryStatus("PENDING");
                shipmentRepository.save(shipment);
            }
            route.getShipments().clear();
        }

        // Soft Delete de la ruta
        route.setStatus("CANCELLED");
        routeRepository.save(route);
    }

    private String determineStrategyKey(String carrierId) {
        // En una aplicación real esto podría basarse en configuraciones de DB. 
        if ("DHL".equalsIgnoreCase(carrierId)) {
            return "dhlStrategy";
        }
        return "localCarrierStrategy"; // Default
    }
}
