package com.smartlogix.inventary.mapper;
import com.smartlogix.inventary.dto.InventoryDTO; import com.smartlogix.inventary.model.Inventory; import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    InventoryDTO toDto(Inventory inventory);
    @Mapping(target = "product", ignore = true) @Mapping(target = "warehouse", ignore = true)
    Inventory toEntity(InventoryDTO dto);
}
