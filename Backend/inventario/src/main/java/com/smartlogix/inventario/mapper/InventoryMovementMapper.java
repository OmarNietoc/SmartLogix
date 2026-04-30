package com.smartlogix.inventario.mapper;
import com.smartlogix.inventario.dto.InventoryMovementDTO; import com.smartlogix.inventario.model.InventoryMovement; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {
    @Mapping(target = "inventoryId", source = "inventory.id")
    InventoryMovementDTO toDto(InventoryMovement movement);
}
