package com.smartlogix.inventary.mapper;

import com.smartlogix.inventary.dto.ProductDTO;
import com.smartlogix.inventary.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product product);

    @Mapping(target = "status", ignore = true)
    Product toEntity(ProductDTO dto);
}
