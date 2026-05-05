package com.smartlogix.inventary.mapper;
import com.smartlogix.inventary.dto.InventoryReservationDTO; import com.smartlogix.inventary.model.InventoryReservation; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryReservationMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    InventoryReservationDTO toDto(InventoryReservation reservation);
}
