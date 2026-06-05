package com.smartlogix.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.inventory.config.SecurityConfig;
import com.smartlogix.inventory.dto.WarehouseDTO;
import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.mapper.WarehouseMapper;
import com.smartlogix.inventory.model.Warehouse;
import com.smartlogix.inventory.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
@Import(SecurityConfig.class)
class WarehouseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean WarehouseService warehouseService;
    @MockBean WarehouseMapper warehouseMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getAllWarehouses_returnsListWith200() throws Exception {
        Warehouse warehouse = buildWarehouse("w1");
        WarehouseDTO dto = buildWarehouseDTO("w1");
        when(warehouseService.getAllWarehouses(COMPANY_ID, null)).thenReturn(List.of(warehouse));
        when(warehouseMapper.toDto(warehouse)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/inventory/warehouses")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("w1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getWarehouseById_found_returns200() throws Exception {
        Warehouse warehouse = buildWarehouse("w1");
        WarehouseDTO dto = buildWarehouseDTO("w1");
        when(warehouseService.getWarehouseById("w1", COMPANY_ID)).thenReturn(warehouse);
        when(warehouseMapper.toDto(warehouse)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/inventory/warehouses/w1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("w1"));
    }

    @Test
    void createWarehouse_validRequest_returns201() throws Exception {
        WarehouseDTO dto = buildWarehouseDTO(null);
        Warehouse entity = buildWarehouse(null);
        Warehouse created = buildWarehouse("w-new");
        WarehouseDTO responseDto = buildWarehouseDTO("w-new");

        when(warehouseMapper.toEntity(any())).thenReturn(entity);
        when(warehouseService.createWarehouse(entity, COMPANY_ID)).thenReturn(created);
        when(warehouseMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/inventory/warehouses")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("w-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void updateWarehouse_validRequest_returns200() throws Exception {
        WarehouseDTO dto = buildWarehouseDTO("w1");
        Warehouse entity = buildWarehouse("w1");
        Warehouse updated = buildWarehouse("w1");
        WarehouseDTO responseDto = buildWarehouseDTO("w1");

        when(warehouseMapper.toEntity(any())).thenReturn(entity);
        when(warehouseService.updateWarehouse(eq("w1"), any(), eq(COMPANY_ID))).thenReturn(updated);
        when(warehouseMapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(put("/smartlogix/inventory/warehouses/w1")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void deleteWarehouse_existing_returns200() throws Exception {
        doNothing().when(warehouseService).deleteWarehouse("w1", COMPANY_ID);

        mockMvc.perform(delete("/smartlogix/inventory/warehouses/w1")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bodega eliminada exitosamente"));
    }

    private Warehouse buildWarehouse(String id) {
        return Warehouse.builder()
                .id(id)
                .companyId(COMPANY_ID)
                .name("Bodega Principal")
                .locationAddress("Av. 123")
                .type(WarehouseType.WAREHOUSE)
                .build();
    }

    private WarehouseDTO buildWarehouseDTO(String id) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(id);
        dto.setName("Bodega Principal");
        dto.setLocationAddress("Av. 123");
        dto.setType("WAREHOUSE");
        return dto;
    }
}
