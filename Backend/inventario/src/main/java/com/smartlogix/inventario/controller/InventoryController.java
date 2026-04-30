package com.smartlogix.inventario.controller;

import com.smartlogix.inventario.dto.InventoryCreationRequestDTO;
import com.smartlogix.inventario.dto.InventoryDTO;
import com.smartlogix.inventario.dto.InventoryMovementDTO;
import com.smartlogix.inventario.dto.MessageResponse;
import com.smartlogix.inventario.dto.StockAdjustmentRequestDTO;
import com.smartlogix.inventario.mapper.InventoryMapper;
import com.smartlogix.inventario.mapper.InventoryMovementMapper;
import com.smartlogix.inventario.model.Inventory;
import com.smartlogix.inventario.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/inventory/stocks")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;
    private final InventoryMovementMapper movementMapper;

    @GetMapping
    public ResponseEntity<MessageResponse<List<InventoryDTO>>> getAllInventory(
            @RequestParam(required = false) String companyId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String warehouseId) {
        List<InventoryDTO> data = inventoryService.getAllInventory(companyId, productId, warehouseId).stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de inventario obtenido exitosamente").data(data).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<InventoryDTO>> getInventoryById(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Inventario obtenido exitosamente")
                .data(inventoryMapper.toDto(inventoryService.getInventoryById(id))).build());
    }

    @PostMapping
    public ResponseEntity<MessageResponse<InventoryDTO>> createInventory(@RequestBody InventoryCreationRequestDTO request) {
        Inventory created = inventoryService.createInventory(request);
        return new ResponseEntity<>(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Inventario creado exitosamente")
                .data(inventoryMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/increase")
    public ResponseEntity<MessageResponse<InventoryDTO>> increaseStock(
            @PathVariable String id,
            @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Stock incrementado exitosamente")
                .data(inventoryMapper.toDto(inventoryService.increaseStock(id, request))).build());
    }

    @PatchMapping("/{id}/decrease")
    public ResponseEntity<MessageResponse<InventoryDTO>> decreaseStock(
            @PathVariable String id,
            @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Stock descontado exitosamente")
                .data(inventoryMapper.toDto(inventoryService.decreaseStock(id, request))).build());
    }

    @GetMapping("/{id}/movements")
    public ResponseEntity<MessageResponse<List<InventoryMovementDTO>>> getMovements(@PathVariable String id) {
        List<InventoryMovementDTO> data = inventoryService.getMovements(id).stream()
                .map(movementMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryMovementDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Movimientos de inventario obtenidos exitosamente").data(data).build());
    }
}
