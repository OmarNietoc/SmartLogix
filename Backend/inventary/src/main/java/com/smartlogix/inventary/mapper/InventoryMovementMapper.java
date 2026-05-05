package com.smartlogix.inventary.mapper;
import com.smartlogix.inventary.dto.InventoryMovementDTO; import com.smartlogix.inventary.model.InventoryMovement; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {
    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryMovementDTO toDto(InventoryMovement movement);
}
