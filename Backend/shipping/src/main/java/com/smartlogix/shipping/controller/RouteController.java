package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.*;
import com.smartlogix.shipping.enums.RouteStatus;
import com.smartlogix.shipping.mapper.RouteMapper;
import com.smartlogix.shipping.model.Route;
import com.smartlogix.shipping.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Shipping - Rutas", description = "Rutas de despacho con soporte OSRM para optimización y Nominatim para geocodificación")
@RestController
@RequestMapping("/smartlogix/shipping/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteMapper routeMapper;

    @Operation(summary = "Listar rutas", description = "Retorna todas las rutas de despacho. Filtrable por empresa y estado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<RouteDTO>>> getAllRoutes(
            @Parameter(description = "UUID de la empresa") @RequestParam(required = false) String companyId,
            @Parameter(description = "Estado de la ruta (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)") @RequestParam(required = false) RouteStatus status) {
        List<RouteDTO> routes = routeService.getAllRoutes(companyId, status).stream()
                .map(routeMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<RouteDTO>> response = MessageResponse.<List<RouteDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de rutas obtenido exitosamente")
                .data(routes)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear ruta y asignar envíos", description = "Crea una ruta de despacho y opcionalmente optimiza el orden de paradas mediante OSRM")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ruta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o envíos no disponibles")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<RouteDTO>> createRoute(@Valid @RequestBody RouteCreationRequestDTO request) {
        Route createdRoute = routeService.createRoute(
                request.getCompanyId(),
                request.getCarrierId(),
                request.getOriginAddress(),
                request.getShipmentIds(),
                request.isOptimizeRoute()
        );

        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Ruta creada exitosamente.")
                .data(routeMapper.toDto(createdRoute))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Generar propuesta de ruta OSRM sin persistir", description = "Calcula distancia y orden óptimo de paradas usando OSRM y Nominatim para los envíos indicados, sin crear una ruta en la base de datos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Propuesta generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error de geocodificación")
    })
    @PostMapping("/generate-proposal")
    public ResponseEntity<MessageResponse<RouteProposalResponseDTO>> generateProposal(
            @Valid @RequestBody RouteProposalRequestDTO request) {
        RouteProposalResponseDTO proposal = routeService.generateProposal(request);
        MessageResponse<RouteProposalResponseDTO> response = MessageResponse.<RouteProposalResponseDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Propuesta de ruta generada exitosamente.")
                .data(proposal)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ruta por ID", description = "Retorna una ruta de despacho por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruta obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<RouteDTO>> getRouteById(
            @Parameter(description = "UUID de la ruta") @PathVariable String id) {
        Route route = routeService.getRouteById(id);
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta obtenida exitosamente")
                .data(routeMapper.toDto(route))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar estado de ruta", description = "Cambia el estado de una ruta de despacho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse<RouteDTO>> updateRouteStatus(
            @Parameter(description = "UUID de la ruta") @PathVariable String id,
            @RequestBody RouteStatus status) {
        Route updated = routeService.updateRouteStatus(id, status);
        MessageResponse<RouteDTO> response = MessageResponse.<RouteDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Estado de ruta actualizado exitosamente")
                .data(routeMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar ruta y liberar envíos", description = "Cancela una ruta (soft delete) y desvincula los envíos asociados, dejándolos disponibles para reasignación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruta cancelada y envíos liberados"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteRoute(
            @Parameter(description = "UUID de la ruta") @PathVariable String id) {
        routeService.deleteRoute(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Ruta cancelada exitosamente y envíos liberados")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
