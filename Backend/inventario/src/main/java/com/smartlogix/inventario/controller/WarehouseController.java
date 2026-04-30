package com.smartlogix.inventario.controller;

import com.smartlogix.inventario.dto.*; import com.smartlogix.inventario.enums.WarehouseType; import com.smartlogix.inventario.mapper.WarehouseMapper; import com.smartlogix.inventario.model.Warehouse; import com.smartlogix.inventario.service.WarehouseService;
import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/smartlogix/inventory/warehouses") @RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService; private final WarehouseMapper warehouseMapper;
    @GetMapping public ResponseEntity<MessageResponse<List<WarehouseDTO>>> getAllWarehouses(@RequestParam(required=false) String companyId, @RequestParam(required=false) WarehouseType type) { List<WarehouseDTO> data=warehouseService.getAllWarehouses(companyId,type).stream().map(warehouseMapper::toDto).collect(Collectors.toList()); return ResponseEntity.ok(MessageResponse.<List<WarehouseDTO>>builder().statusCode(HttpStatus.OK.value()).message("Listado de bodegas obtenido exitosamente").data(data).build()); }
    @GetMapping("/{id}") public ResponseEntity<MessageResponse<WarehouseDTO>> getWarehouseById(@PathVariable String id) { return ResponseEntity.ok(MessageResponse.<WarehouseDTO>builder().statusCode(HttpStatus.OK.value()).message("Bodega obtenida exitosamente").data(warehouseMapper.toDto(warehouseService.getWarehouseById(id))).build()); }
    @PostMapping public ResponseEntity<MessageResponse<WarehouseDTO>> createWarehouse(@RequestBody WarehouseDTO dto) { Warehouse created=warehouseService.createWarehouse(warehouseMapper.toEntity(dto)); return new ResponseEntity<>(MessageResponse.<WarehouseDTO>builder().statusCode(HttpStatus.CREATED.value()).message("Bodega creada exitosamente").data(warehouseMapper.toDto(created)).build(), HttpStatus.CREATED); }
    @PutMapping("/{id}") public ResponseEntity<MessageResponse<WarehouseDTO>> updateWarehouse(@PathVariable String id, @RequestBody WarehouseDTO dto) { Warehouse updated=warehouseService.updateWarehouse(id, warehouseMapper.toEntity(dto)); return ResponseEntity.ok(MessageResponse.<WarehouseDTO>builder().statusCode(HttpStatus.OK.value()).message("Bodega actualizada exitosamente").data(warehouseMapper.toDto(updated)).build()); }
    @DeleteMapping("/{id}") public ResponseEntity<MessageResponse<Void>> deleteWarehouse(@PathVariable String id) { warehouseService.deleteWarehouse(id); return ResponseEntity.ok(MessageResponse.<Void>builder().statusCode(HttpStatus.OK.value()).message("Bodega eliminada exitosamente").data(null).build()); }
}
