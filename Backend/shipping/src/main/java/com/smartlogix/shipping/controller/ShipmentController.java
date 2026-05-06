package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.mapper.ShipmentMapper;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.service.ShipmentService;
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

@Tag(name = "Shipping - Envíos", description = "Gestión de envíos individuales con número de tracking. Los envíos se crean automáticamente vía RabbitMQ desde ms-order")
@RestController
@RequestMapping("/smartlogix/shipping/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @Operation(summary = "Listar envíos", description = "Retorna todos los envíos. Filtrable por estado de entrega")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<ShipmentDTO>>> getAllShipments(
            @Parameter(description = "Estado de entrega (PENDING, IN_TRANSIT, DELIVERED, FAILED)") @RequestParam(required = false) DeliveryStatus deliveryStatus) {
        List<ShipmentDTO> shipments = shipmentService.getAllShipments(deliveryStatus).stream()
                .map(shipmentMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<ShipmentDTO>> response = MessageResponse.<List<ShipmentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de envíos obtenido")
                .data(shipments)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener envío por ID", description = "Retorna un envío por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envío obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> getShipmentById(
            @Parameter(description = "UUID del envío") @PathVariable String id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipmentMapper.toDto(shipment))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar envío por número de tracking", description = "Retorna un envío usando su número de tracking único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envío obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Número de tracking no encontrado")
    })
    @GetMapping("/tracking/{tracking_number}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> getShipmentByTrackingNumber(
            @Parameter(description = "Número de tracking del envío", example = "TRK-20240506-001") @PathVariable("tracking_number") String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipmentMapper.toDto(shipment))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear envío manualmente", description = "Crea un envío de forma manual. Normalmente los envíos se crean automáticamente al confirmar una reserva de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Envío creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<ShipmentDTO>> createShipment(@RequestBody ShipmentDTO shipmentDto) {
        Shipment created = shipmentService.createShipment(shipmentMapper.toEntity(shipmentDto));
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Envío creado exitosamente")
                .data(shipmentMapper.toDto(created))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar estado de entrega", description = "Cambia el estado de entrega de un envío")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponse<ShipmentDTO>> updateShipmentStatus(
            @Parameter(description = "UUID del envío") @PathVariable String id,
            @RequestBody DeliveryStatus status) {
        Shipment updated = shipmentService.updateShipmentStatus(id, status);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Estado de envío actualizado exitosamente")
                .data(shipmentMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar envío", description = "Elimina un envío del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Envío eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteShipment(
            @Parameter(description = "UUID del envío") @PathVariable String id) {
        shipmentService.deleteShipment(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío eliminado exitosamente")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
