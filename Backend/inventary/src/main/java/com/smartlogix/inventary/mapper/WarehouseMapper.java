package com.smartlogix.inventary.mapper;
import com.smartlogix.inventary.dto.WarehouseDTO; import com.smartlogix.inventary.model.Warehouse; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring") public interface WarehouseMapper { WarehouseDTO toDto(Warehouse warehouse); Warehouse toEntity(WarehouseDTO dto); }
