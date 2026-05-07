package com.smartlogix.inventory.mapper;

import com.smartlogix.inventory.dto.WarehouseDTO;
import com.smartlogix.inventory.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDTO toDto(Warehouse warehouse);

    @Mapping(target = "status", ignore = true)
    Warehouse toEntity(WarehouseDTO dto);
}
