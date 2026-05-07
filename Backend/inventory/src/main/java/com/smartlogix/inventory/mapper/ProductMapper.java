package com.smartlogix.inventory.mapper;

import com.smartlogix.inventory.dto.ProductDTO;
import com.smartlogix.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);

    @Mapping(target = "status", ignore = true)
    Product toEntity(ProductDTO dto);
}
