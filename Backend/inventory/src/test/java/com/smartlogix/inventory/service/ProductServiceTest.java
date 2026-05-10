package com.smartlogix.inventory.service;

import com.smartlogix.inventory.exception.ProductNotFoundException;
import com.smartlogix.inventory.model.Product;
import com.smartlogix.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    @Test
    void getAllProducts_usesCompanyFilter() {
        when(productRepository.findByCompanyId("company-1")).thenReturn(List.of(product("p1", "company-1", "SKU-1")));

        List<Product> result = productService.getAllProducts("company-1");

        assertThat(result).hasSize(1);
        verify(productRepository).findByCompanyId("company-1");
        verify(productRepository, never()).findAll();
    }

    @Test
    void getProductById_rejectsOtherCompanyProductsAsNotFound() {
        when(productRepository.findByIdAndCompanyId("p1", "company-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("p1", "company-1"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createProduct_ignoresBodyCompanyIdAndUsesAuthenticatedCompany() {
        Product incoming = product("new", "company-evil", "SKU-1");
        when(productRepository.existsBySkuAndCompanyId("SKU-1", "company-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.createProduct(incoming, "company-1");

        assertThat(saved.getCompanyId()).isEqualTo("company-1");
        verify(productRepository).existsBySkuAndCompanyId("SKU-1", "company-1");
    }

    @Test
    void createProduct_rejectsDuplicateSkuWithinSameCompany() {
        Product incoming = product("new", null, "SKU-1");
        when(productRepository.existsBySkuAndCompanyId("SKU-1", "company-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(incoming, "company-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SKU-1");
    }

    private Product product(String id, String companyId, String sku) {
        return Product.builder()
                .id(id)
                .companyId(companyId)
                .sku(sku)
                .name("Producto " + sku)
                .price(BigDecimal.TEN)
                .build();
    }
}
