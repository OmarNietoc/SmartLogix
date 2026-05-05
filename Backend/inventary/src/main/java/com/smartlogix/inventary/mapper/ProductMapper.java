package com.smartlogix.inventary.mapper;
import com.smartlogix.inventary.dto.ProductDTO; import com.smartlogix.inventary.model.Product; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring") public interface ProductMapper { ProductDTO toDto(Product product); Product toEntity(ProductDTO dto); }
