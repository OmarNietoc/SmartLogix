package com.smartlogix.shipping.controller;

import com.smartlogix.shipping.dto.MessageResponse;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/smartlogix/shipping/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<MessageResponse<List<Shipment>>> getAllShipments() {
        List<Shipment> shipments = shipmentService.getAllShipments();
        MessageResponse<List<Shipment>> response = MessageResponse.<List<Shipment>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de envíos obtenido")
                .data(shipments)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<Shipment>> getShipmentById(@PathVariable String id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        MessageResponse<Shipment> response = MessageResponse.<Shipment>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío obtenido con éxito")
                .data(shipment)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponse<Shipment>> createShipment(@RequestBody Shipment shipment) {
        Shipment created = shipmentService.createShipment(shipment);
        MessageResponse<Shipment> response = MessageResponse.<Shipment>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Envío creado exitosamente")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<Shipment>> updateShipment(@PathVariable String id, @RequestBody Shipment shipmentDetails) {
        Shipment updated = shipmentService.updateShipment(id, shipmentDetails);
        MessageResponse<Shipment> response = MessageResponse.<Shipment>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Envío actualizado exitosamente")
                .data(updated)
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
