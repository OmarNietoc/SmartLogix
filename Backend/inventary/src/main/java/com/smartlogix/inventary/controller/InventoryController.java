package com.smartlogix.inventary.controller;

import com.smartlogix.inventary.dto.InventoryCreationRequestDTO;
import com.smartlogix.inventary.dto.InventoryDTO;
import com.smartlogix.inventary.dto.InventoryMovementDTO;
import com.smartlogix.inventary.dto.MessageResponse;
import com.smartlogix.inventary.dto.StockAdjustmentRequestDTO;
import com.smartlogix.inventary.mapper.InventoryMapper;
import com.smartlogix.inventary.mapper.InventoryMovementMapper;
import com.smartlogix.inventary.model.Inventory;
import com.smartlogix.inventary.service.InventoryService;
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

@Tag(name = "Inventory - Stock", description = "Gestión de stock y registros de inventario")
@RestController
@RequestMapping("/smartlogix/inventory/stocks")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;
    private final InventoryMovementMapper movementMapper;

    @Operation(summary = "Listar stock", description = "Retorna todos los registros de inventario. Filtrable por empresa, producto o bodega")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<InventoryDTO>>> getAllInventory(
            @Parameter(description = "UUID de la empresa") @RequestParam(required = false) String companyId,
            @Parameter(description = "UUID del producto") @RequestParam(required = false) String productId,
            @Parameter(description = "UUID de la bodega") @RequestParam(required = false) String warehouseId) {
        List<InventoryDTO> data = inventoryService.getAllInventory(companyId, productId, warehouseId).stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de stock obtenido exitosamente").data(data).build());
    }

    @Operation(summary = "Obtener stock por ID", description = "Retorna un registro de inventario por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Registro de inventario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<InventoryDTO>> getInventoryById(
            @Parameter(description = "UUID del registro de inventario") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Stock obtenido exitosamente")
                .data(inventoryMapper.toDto(inventoryService.getInventoryById(id))).build());
    }

    @Operation(summary = "Crear registro de inventario", description = "Registra un nuevo ítem de stock en una bodega para un producto dado")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Stock creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<InventoryDTO>> createInventory(@RequestBody InventoryCreationRequestDTO request) {
        Inventory created = inventoryService.createInventory(request);
        return new ResponseEntity<>(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Stock creado exitosamente")
                .data(inventoryMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Incrementar stock", description = "Aumenta la cantidad disponible de un registro de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock incrementado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Registro de inventario no encontrado")
    })
    @PatchMapping("/{id}/increase")
    public ResponseEntity<MessageResponse<InventoryDTO>> increaseStock(
            @Parameter(description = "UUID del registro de inventario") @PathVariable String id,
            @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Stock incrementado exitosamente")
                .data(inventoryMapper.toDto(inventoryService.increaseStock(id, request))).build());
    }

    @Operation(summary = "Descontar stock", description = "Reduce la cantidad disponible de un registro de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock descontado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente para el descuento"),
        @ApiResponse(responseCode = "404", description = "Registro de inventario no encontrado")
    })
    @PatchMapping("/{id}/decrease")
    public ResponseEntity<MessageResponse<InventoryDTO>> decreaseStock(
            @Parameter(description = "UUID del registro de inventario") @PathVariable String id,
            @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(MessageResponse.<InventoryDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Stock descontado exitosamente")
                .data(inventoryMapper.toDto(inventoryService.decreaseStock(id, request))).build());
    }

    @Operation(summary = "Historial de movimientos", description = "Retorna todos los movimientos de stock registrados para un inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimientos obtenidos exitosamente"),
        @ApiResponse(responseCode = "404", description = "Registro de inventario no encontrado")
    })
    @GetMapping("/{id}/movements")
    public ResponseEntity<MessageResponse<List<InventoryMovementDTO>>> getMovements(
            @Parameter(description = "UUID del registro de inventario") @PathVariable String id) {
        List<InventoryMovementDTO> data = inventoryService.getMovements(id).stream()
                .map(movementMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<InventoryMovementDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Movimientos de stock obtenidos exitosamente").data(data).build());
    }
}
