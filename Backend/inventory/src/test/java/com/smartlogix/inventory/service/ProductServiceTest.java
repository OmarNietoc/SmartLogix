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

    @Test
    void getProductBySku_returnsProductForCompany() {
        when(productRepository.findBySkuAndCompanyId("SKU-1", "company-1"))
                .thenReturn(Optional.of(product("p1", "company-1", "SKU-1")));

        Product result = productService.getProductBySku("SKU-1", "company-1");

        assertThat(result.getId()).isEqualTo("p1");
    }

    @Test
    void updateProduct_updatesMutableFieldsAndRejectsDuplicateSku() {
        Product existing = product("p1", "company-1", "SKU-1");
        Product update = product("ignored", "other", "SKU-2");
        update.setName("Nuevo nombre");
        update.setPrice(BigDecimal.valueOf(25));
        when(productRepository.findByIdAndCompanyId("p1", "company-1")).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuAndCompanyId("SKU-2", "company-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.updateProduct("p1", update, "company-1");

        assertThat(saved.getSku()).isEqualTo("SKU-2");
        assertThat(saved.getName()).isEqualTo("Nuevo nombre");
        assertThat(saved.getCompanyId()).isEqualTo("company-1");
    }

    @Test
    void deleteProduct_marksProductInactive() {
        Product existing = product("p1", "company-1", "SKU-1");
        when(productRepository.findByIdAndCompanyId("p1", "company-1")).thenReturn(Optional.of(existing));

        productService.deleteProduct("p1", "company-1");

        assertThat(existing.getStatus()).isEqualTo("INACTIVE");
        verify(productRepository).save(existing);
    }

    @Test
    void createProduct_validatesCompanySkuNameAndPrice() {
        assertThatThrownBy(() -> productService.createProduct(product("p1", null, "SKU-1"), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X-Company-Id");

        Product invalidSku = product("p1", "company-1", " ");
        assertThatThrownBy(() -> productService.createProduct(invalidSku, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sku");

        Product invalidName = product("p1", "company-1", "SKU-1");
        invalidName.setName(" ");
        assertThatThrownBy(() -> productService.createProduct(invalidName, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");

        Product invalidPrice = product("p1", "company-1", "SKU-1");
        invalidPrice.setPrice(BigDecimal.valueOf(-1));
        assertThatThrownBy(() -> productService.createProduct(invalidPrice, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price");
    }

    private Product product(String id, String companyId, String sku) {
        return Product.builder()
                .id(id)
                .companyId(companyId)
                .sku(sku)
                .name("Producto " + sku)
                .price(BigDecimal.TEN)
                .status("ACTIVE")
                .build();
    }
}
