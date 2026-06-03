package com.smartlogix.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.inventory.config.SecurityConfig;
import com.smartlogix.inventory.dto.InventoryCreationRequestDTO;
import com.smartlogix.inventory.dto.InventoryDTO;
import com.smartlogix.inventory.dto.StockAdjustmentRequestDTO;
import com.smartlogix.inventory.mapper.InventoryMapper;
import com.smartlogix.inventory.mapper.InventoryMovementMapper;
import com.smartlogix.inventory.model.Inventory;
import com.smartlogix.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean InventoryService inventoryService;
    @MockBean InventoryMapper inventoryMapper;
    @MockBean InventoryMovementMapper movementMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllInventory_returnsListWith200() throws Exception {
        // arrange
        Inventory inv = new Inventory();
        InventoryDTO dto = buildInventoryDTO("inv-1");
        when(inventoryService.getAllInventory(COMPANY_ID, null, null)).thenReturn(List.of(inv));
        when(inventoryMapper.toDto(inv)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/inventory/stocks")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("inv-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getInventoryById_found_returns200() throws Exception {
        // arrange
        Inventory inv = new Inventory();
        InventoryDTO dto = buildInventoryDTO("inv-1");
        when(inventoryService.getInventoryById("inv-1")).thenReturn(inv);
        when(inventoryMapper.toDto(inv)).thenReturn(dto);

        // act & assert
        mockMvc.perform(get("/smartlogix/inventory/stocks/inv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("inv-1"));
    }

    @Test
    void createInventory_validRequest_returns201() throws Exception {
        // arrange
        InventoryCreationRequestDTO request = new InventoryCreationRequestDTO("prod-1", "wh-1", 50);
        Inventory created = new Inventory();
        InventoryDTO dto = buildInventoryDTO("inv-new");
        when(inventoryService.createInventory(any(), eq(COMPANY_ID))).thenReturn(created);
        when(inventoryMapper.toDto(created)).thenReturn(dto);

        // act & assert
        mockMvc.perform(post("/smartlogix/inventory/stocks")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("inv-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void increaseStock_validRequest_returns200() throws Exception {
        // arrange
        StockAdjustmentRequestDTO request = new StockAdjustmentRequestDTO(10, "Recepción");
        Inventory updated = new Inventory();
        InventoryDTO dto = buildInventoryDTO("inv-1");
        when(inventoryService.increaseStock(eq("inv-1"), any())).thenReturn(updated);
        when(inventoryMapper.toDto(updated)).thenReturn(dto);

        // act & assert
        mockMvc.perform(patch("/smartlogix/inventory/stocks/inv-1/increase")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock incrementado exitosamente"));
    }

    @Test
    void decreaseStock_validRequest_returns200() throws Exception {
        // arrange
        StockAdjustmentRequestDTO request = new StockAdjustmentRequestDTO(5, "Venta");
        Inventory updated = new Inventory();
        InventoryDTO dto = buildInventoryDTO("inv-1");
        when(inventoryService.decreaseStock(eq("inv-1"), any())).thenReturn(updated);
        when(inventoryMapper.toDto(updated)).thenReturn(dto);

        // act & assert
        mockMvc.perform(patch("/smartlogix/inventory/stocks/inv-1/decrease")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock descontado exitosamente"));
    }

    @Test
    void getMovements_returnsEmptyList() throws Exception {
        // arrange
        when(inventoryService.getMovements("inv-1")).thenReturn(List.of());

        // act & assert
        mockMvc.perform(get("/smartlogix/inventory/stocks/inv-1/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    private InventoryDTO buildInventoryDTO(String id) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(id);
        dto.setProductId("prod-1");
        dto.setWarehouseId("wh-1");
        dto.setStockAvailable(50);
        dto.setStockReserved(0);
        return dto;
    }
}
