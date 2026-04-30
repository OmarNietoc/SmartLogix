package com.smartlogix.inventario.mapper;
import com.smartlogix.inventario.dto.ProductDTO; import com.smartlogix.inventario.model.Product; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring") public interface ProductMapper { ProductDTO toDto(Product product); Product toEntity(ProductDTO dto); }
