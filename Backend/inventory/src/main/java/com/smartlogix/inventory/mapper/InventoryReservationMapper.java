package com.smartlogix.inventory.mapper;
import com.smartlogix.inventory.dto.InventoryReservationDTO; import com.smartlogix.inventory.model.InventoryReservation; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryReservationMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    InventoryReservationDTO toDto(InventoryReservation reservation);
}
