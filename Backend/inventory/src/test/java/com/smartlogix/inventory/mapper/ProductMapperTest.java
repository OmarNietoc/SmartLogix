package com.smartlogix.inventory.mapper;

import com.smartlogix.inventory.dto.ProductDTO;
import com.smartlogix.inventory.mapper.ProductMapperImpl;
import com.smartlogix.inventory.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapperImpl();

    @Test
    @DisplayName("toDto maps all fields from Product to ProductDTO")
    void toDto_mapsAllFields() {
        Product product = Product.builder()
                .id("p1")
                .companyId("company-1")
                .sku("SKU-001")
                .name("Producto Test")
                .price(new BigDecimal("9990.00"))
                .status("ACTIVE")
                .build();

        ProductDTO dto = mapper.toDto(product);

        assertThat(dto.getId()).isEqualTo("p1");
        assertThat(dto.getCompanyId()).isEqualTo("company-1");
        assertThat(dto.getSku()).isEqualTo("SKU-001");
        assertThat(dto.getName()).isEqualTo("Producto Test");
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("9990.00"));
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("toDto returns null when product is null")
    void toDto_null_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("toEntity maps all non-ignored fields from ProductDTO to Product")
    void toEntity_mapsFields() {
        ProductDTO dto = ProductDTO.builder()
                .id("p1")
                .companyId("company-1")
                .sku("SKU-001")
                .name("Producto Test")
                .price(new BigDecimal("9990.00"))
                .build();

        Product product = mapper.toEntity(dto);

        assertThat(product.getId()).isEqualTo("p1");
        assertThat(product.getCompanyId()).isEqualTo("company-1");
        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Producto Test");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("9990.00"));
    }

    @Test
    @DisplayName("toEntity returns null when dto is null")
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
