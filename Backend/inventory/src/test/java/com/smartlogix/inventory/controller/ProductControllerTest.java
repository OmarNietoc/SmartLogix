package com.smartlogix.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.inventory.config.SecurityConfig;
import com.smartlogix.inventory.dto.ProductDTO;
import com.smartlogix.inventory.mapper.ProductMapper;
import com.smartlogix.inventory.model.Product;
import com.smartlogix.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductService productService;
    @MockBean ProductMapper productMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllProducts_returnsListWith200() throws Exception {
        // arrange
        Product product = new Product();
        ProductDTO dto = buildProductDTO("prod-1", "PROD-001");
        when(productService.getAllProducts(COMPANY_ID)).thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/inventory/products")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("prod-1"))
                .andExpect(jsonPath("$.data[0].sku").value("PROD-001"));
    }

    @Test
    void getProductById_found_returns200() throws Exception {
        // arrange
        Product product = new Product();
        ProductDTO dto = buildProductDTO("prod-1", "PROD-001");
        when(productService.getProductById("prod-1", COMPANY_ID)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/inventory/products/prod-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("prod-1"));
    }

    @Test
    void createProduct_validRequest_returns201() throws Exception {
        // arrange
        ProductDTO request = buildProductDTO(null, "PROD-NEW");
        Product entity = new Product();
        Product created = new Product();
        ProductDTO responseDto = buildProductDTO("prod-new", "PROD-NEW");

        when(productMapper.toEntity(any())).thenReturn(entity);
        when(productService.createProduct(entity, COMPANY_ID)).thenReturn(created);
        when(productMapper.toDto(created)).thenReturn(responseDto);

        // act & assert
        mockMvc.perform(post("/smartlogix/inventory/products")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("prod-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void updateProduct_validRequest_returns200() throws Exception {
        // arrange
        ProductDTO request = buildProductDTO("prod-1", "PROD-001");
        Product entity = new Product();
        Product updated = new Product();
        ProductDTO responseDto = buildProductDTO("prod-1", "PROD-001");

        when(productMapper.toEntity(any())).thenReturn(entity);
        when(productService.updateProduct(eq("prod-1"), eq(entity), eq(COMPANY_ID))).thenReturn(updated);
        when(productMapper.toDto(updated)).thenReturn(responseDto);

        // act & assert
        mockMvc.perform(put("/smartlogix/inventory/products/prod-1")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("prod-1"));
    }

    @Test
    void deleteProduct_existing_returns200() throws Exception {
        // arrange
        doNothing().when(productService).deleteProduct("prod-1", COMPANY_ID);

        // act & assert
        mockMvc.perform(delete("/smartlogix/inventory/products/prod-1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto eliminado exitosamente"));
    }

    private ProductDTO buildProductDTO(String id, String sku) {
        ProductDTO dto = new ProductDTO();
        dto.setId(id);
        dto.setSku(sku);
        dto.setName("Producto Test");
        dto.setPrice(new BigDecimal("1500"));
        dto.setCompanyId(COMPANY_ID);
        return dto;
    }
}
