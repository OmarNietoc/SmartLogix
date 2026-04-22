package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.service.RouteService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/smartlogix/shipping/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<MessageResponse<List<Route>>> getAllRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        MessageResponse<List<Route>> response = MessageResponse.<List<Route>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de rutas obtenido exitosamente")
                .data(routes)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponse<Route>> createRoute(@RequestBody RouteCreationRequest request) {
        Route createdRoute = routeService.createRoute(
                request.getCompanyId(),
                request.getCarrierId(),
                request.getOriginAddress(),
                request.getShipmentIds()
        );

        MessageResponse<Route> response = MessageResponse.<Route>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Ruta creada exitosamente.")
                .data(createdRoute)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<Route>> getRouteById(@PathVariable String id) {
        Route route = routeService.getRouteById(id);
        
        MessageResponse<Route> response = MessageResponse.<Route>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta obtenida exitosamente")
                .data(route)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<Route>> updateRoute(@PathVariable String id, @RequestBody Route routeDetails) {
        Route updated = routeService.updateRoute(id, routeDetails);
        MessageResponse<Route> response = MessageResponse.<Route>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta actualizada exitosamente")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta cancelada exitosamente y envíos liberados")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @Data
    public static class RouteCreationRequest {
        private String companyId;
        private String carrierId;
        private String originAddress;
        private List<String> shipmentIds;
    }
}
