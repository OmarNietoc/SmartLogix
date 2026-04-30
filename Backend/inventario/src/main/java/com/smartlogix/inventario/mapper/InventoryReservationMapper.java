package com.smartlogix.inventario.mapper;
import com.smartlogix.inventario.dto.InventoryReservationDTO; import com.smartlogix.inventario.model.InventoryReservation; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryReservationMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    InventoryReservationDTO toDto(InventoryReservation reservation);
}
