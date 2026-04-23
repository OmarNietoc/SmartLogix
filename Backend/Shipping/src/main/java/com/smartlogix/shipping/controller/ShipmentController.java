package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.dto.ShipmentDTO;
import com.smartlogix.shipping.mapper.ShipmentMapper;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/shipping/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @GetMapping
    public ResponseEntity<MessageResponse<List<ShipmentDTO>>> getAllShipments() {
        List<ShipmentDTO> shipments = shipmentService.getAllShipments().stream()
                .map(shipmentMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<ShipmentDTO>> response = MessageResponse.<List<ShipmentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de envíos obtenido")
                .data(shipments)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> getShipmentById(@PathVariable String id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipmentMapper.toDto(shipment))
                .build();
        return ResponseEntity.ok(response);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<ShipmentDTO>> updateShipment(@PathVariable String id, @RequestBody ShipmentDTO shipmentDto) {
        Shipment updated = shipmentService.updateShipment(id, shipmentMapper.toEntity(shipmentDto));
        MessageResponse<ShipmentDTO> response = MessageResponse.<ShipmentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío actualizado exitosamente")
                .data(shipmentMapper.toDto(updated))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteShipment(@PathVariable String id) {
        shipmentService.deleteShipment(id);
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío eliminado exitosamente")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
