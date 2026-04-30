package com.smartlogix.inventario.mapper;
import com.smartlogix.inventario.dto.WarehouseDTO; import com.smartlogix.inventario.model.Warehouse; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring") public interface WarehouseMapper { WarehouseDTO toDto(Warehouse warehouse); Warehouse toEntity(WarehouseDTO dto); }
