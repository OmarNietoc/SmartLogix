package com.smartlogix.inventory.mapper;
import com.smartlogix.inventory.dto.InventoryMovementDTO; import com.smartlogix.inventory.model.InventoryMovement; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {
    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryMovementDTO toDto(InventoryMovement movement);
}
