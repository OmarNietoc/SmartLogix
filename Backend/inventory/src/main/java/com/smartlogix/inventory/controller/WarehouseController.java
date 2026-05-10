package com.smartlogix.inventory.controller;

import com.smartlogix.inventory.dto.*;
import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.mapper.WarehouseMapper;
import com.smartlogix.inventory.model.Warehouse;
import com.smartlogix.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Inventory - Bodegas", description = "Gestión de bodegas y almacenes por empresa")
@RestController
@RequestMapping("/smartlogix/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    @Operation(summary = "Listar bodegas", description = "Retorna las bodegas de la empresa autenticada. Filtrable por tipo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<WarehouseDTO>>> getAllWarehouses(
            @Parameter(description = "UUID de la empresa") @RequestHeader("X-Company-Id") String companyId,
            @Parameter(description = "Tipo de bodega (WAREHOUSE, RETAIL_STORE)") @RequestParam(required = false) WarehouseType type) {
        List<WarehouseDTO> data = warehouseService.getAllWarehouses(companyId, type).stream()
                .map(warehouseMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<WarehouseDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de bodegas obtenido exitosamente").data(data).build());
    }

    @Operation(summary = "Obtener bodega por ID", description = "Retorna una bodega por UUID dentro de la empresa autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bodega obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<WarehouseDTO>> getWarehouseById(
            @Parameter(description = "UUID de la bodega") @PathVariable String id,
            @RequestHeader("X-Company-Id") String companyId) {
        return ResponseEntity.ok(MessageResponse.<WarehouseDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Bodega obtenida exitosamente")
                .data(warehouseMapper.toDto(warehouseService.getWarehouseById(id, companyId))).build());
    }

    @Operation(summary = "Crear bodega", description = "Registra una nueva bodega para la empresa autenticada")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bodega creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<WarehouseDTO>> createWarehouse(
            @Valid @RequestBody WarehouseDTO dto,
            @RequestHeader("X-Company-Id") String companyId) {
        Warehouse created = warehouseService.createWarehouse(warehouseMapper.toEntity(dto), companyId);
        return new ResponseEntity<>(MessageResponse.<WarehouseDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Bodega creada exitosamente")
                .data(warehouseMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar bodega", description = "Modifica los datos de una bodega existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bodega actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<WarehouseDTO>> updateWarehouse(
            @Parameter(description = "UUID de la bodega") @PathVariable String id,
            @Valid @RequestBody WarehouseDTO dto,
            @RequestHeader("X-Company-Id") String companyId) {
        Warehouse updated = warehouseService.updateWarehouse(id, warehouseMapper.toEntity(dto), companyId);
        return ResponseEntity.ok(MessageResponse.<WarehouseDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Bodega actualizada exitosamente")
                .data(warehouseMapper.toDto(updated)).build());
    }

    @Operation(summary = "Eliminar bodega", description = "Elimina una bodega del sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bodega eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Bodega no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteWarehouse(
            @Parameter(description = "UUID de la bodega") @PathVariable String id,
            @RequestHeader("X-Company-Id") String companyId) {
        warehouseService.deleteWarehouse(id, companyId);
        return ResponseEntity.ok(MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value()).message("Bodega eliminada exitosamente").data(null).build());
    }
}
