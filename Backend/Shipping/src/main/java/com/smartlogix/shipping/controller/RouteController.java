package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.dto.RouteCreationRequestDTO;
import com.smartlogix.shipping.dto.RouteDTO;
import com.smartlogix.shipping.mapper.RouteMapper;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/shipping/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteMapper routeMapper;

    @GetMapping
    public ResponseEntity<MessageResponse<List<RouteDTO>>> getAllRoutes() {
        List<RouteDTO> routes = routeService.getAllRoutes().stream()
                .map(routeMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<RouteDTO>> response = MessageResponse.<List<RouteDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de rutas obtenido exitosamente")
                .data(routes)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponse<RouteDTO>> createRoute(@RequestBody RouteCreationRequestDTO request) {
        Route createdRoute = routeService.createRoute(
                request.getCompanyId(),
                request.getCarrierId(),
                request.getOriginAddress(),
                request.getShipmentIds()
        );

        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Ruta creada exitosamente.")
                .data(routeMapper.toDto(createdRoute))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<RouteDTO>> getRouteById(@PathVariable String id) {
        Route route = routeService.getRouteById(id);
        
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta obtenida exitosamente")
                .data(routeMapper.toDto(route))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<RouteDTO>> updateRoute(@PathVariable String id, @RequestBody RouteDTO routeDto) {
        Route updated = routeService.updateRoute(id, routeMapper.toEntity(routeDto));
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta actualizada exitosamente")
                .data(routeMapper.toDto(updated))
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
}
