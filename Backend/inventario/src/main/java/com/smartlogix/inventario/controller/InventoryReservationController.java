package com.smartlogix.inventario.controller;

import com.smartlogix.inventario.dto.InventoryReservationDTO;
import com.smartlogix.inventario.dto.MessageResponse;
import com.smartlogix.inventario.dto.StockReservationRequestDTO;
import com.smartlogix.inventario.enums.ReservationStatus;
import com.smartlogix.inventario.mapper.InventoryReservationMapper;
import com.smartlogix.inventario.model.InventoryReservation;
import com.smartlogix.inventario.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/inventory/reservations")
@RequiredArgsConstructor
public class InventoryReservationController {
    private final InventoryReservationService reservationService;
    private final InventoryReservationMapper reservationMapper;

    @GetMapping
    public ResponseEntity<MessageResponse<List<InventoryReservationDTO>>> getAllReservations(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) ReservationStatus status) {
        List<InventoryReservationDTO> data = reservationService.getAllReservations(orderId, status).stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryReservationDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de reservas obtenido exitosamente").data(data).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> getReservationById(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva obtenida exitosamente")
                .data(reservationMapper.toDto(reservationService.getReservationById(id))).build());
    }

    @PostMapping
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> reserveStock(@RequestBody StockReservationRequestDTO request) {
        InventoryReservation created = reservationService.reserveStock(request);
        return new ResponseEntity<>(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Stock reservado exitosamente")
                .data(reservationMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/compensate")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> compensateReservation(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva compensada exitosamente")
                .data(reservationMapper.toDto(reservationService.compensateReservation(id))).build());
    }

    @PatchMapping("/{id}/confirm-output")
    public ResponseEntity<MessageResponse<InventoryReservationDTO>> confirmReservationAsOutput(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryReservationDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Reserva confirmada como salida definitiva")
                .data(reservationMapper.toDto(reservationService.confirmReservationAsOutput(id))).build());
    }
}
