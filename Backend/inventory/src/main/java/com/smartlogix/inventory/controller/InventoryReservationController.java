package com.smartlogix.inventory.controller;

import com.smartlogix.inventory.dto.InventoryReservationDTO;
import com.smartlogix.inventory.dto.MessageResponse;
import com.smartlogix.inventory.dto.StockReservationRequestDTO;
import com.smartlogix.inventory.enums.ReservationStatus;
import com.smartlogix.inventory.mapper.InventoryReservationMapper;
import com.smartlogix.inventory.model.InventoryReservation;
import com.smartlogix.inventory.service.InventoryReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Inventory - Reservas", description = "Reservas de stock para el patrón Saga (coreografía con ms-order)")
@RestController
@RequestMapping("/smartlogix/inventory/reservations")
@RequiredArgsConstructor
public class InventoryReservationController {
    private final InventoryReservationService reservationService;
    private final InventoryReservationMapper reservationMapper;

    @Operation(summary = "Listar reservas", description = "Retorna todas las reservas de stock. Filtrable por pedido o estado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<InventoryReservationDTO>>> getAllReservations(
            @Parameter(description = "UUID del pedido asociado") @RequestParam(required = false) String orderId,
            @Parameter(description = "Estado de la reserva (PENDING, CONFIRMED, COMPENSATED)") @RequestParam(required = false) ReservationStatus status) {
        List<InventoryReservationDTO> data = reservationService.getAllReservations(orderId, status).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryReservationDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de reservas obtenido exitosamente").data(data).build());
    }

    @Operation(summary = "Obtener reserva por ID", description = "Retorna una reserva de stock por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> getReservationById(
            @Parameter(description = "UUID de la reserva") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva obtenida exitosamente")
                .data(reservationMapper.toDto(reservationService.getReservationById(id))).build());
    }

    @Operation(summary = "Reservar stock", description = "Crea una reserva de stock para un pedido (paso del patrón Saga)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Stock reservado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> reserveStock(@RequestBody StockReservationRequestDTO request) {
        InventoryReservation created = reservationService.reserveStock(request);
        return new ResponseEntity<>(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Stock reservado exitosamente")
                .data(reservationMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Compensar reserva (rollback Saga)", description = "Libera el stock reservado cuando el pedido falla — transacción de compensación Saga")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva compensada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PatchMapping("/{id}/compensate")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> compensateReservation(
            @Parameter(description = "UUID de la reserva") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva compensada exitosamente")
                .data(reservationMapper.toDto(reservationService.compensateReservation(id))).build());
    }

    @Operation(summary = "Confirmar salida definitiva de stock", description = "Convierte la reserva en salida real del inventario al confirmar el despacho")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reserva confirmada como salida definitiva"),
        @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PatchMapping("/{id}/confirm-output")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> confirmReservationAsOutput(
            @Parameter(description = "UUID de la reserva") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva confirmada como salida definitiva")
                .data(reservationMapper.toDto(reservationService.confirmReservationAsOutput(id))).build());
    }
}
