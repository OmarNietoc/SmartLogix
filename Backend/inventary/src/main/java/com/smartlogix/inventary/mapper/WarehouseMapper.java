package com.smartlogix.inventary.mapper;

import com.smartlogix.inventary.dto.WarehouseDTO;
import com.smartlogix.inventary.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDTO toDto(Warehouse warehouse);

    @Mapping(target = "status", ignore = true)
    Warehouse toEntity(WarehouseDTO dto);
}
