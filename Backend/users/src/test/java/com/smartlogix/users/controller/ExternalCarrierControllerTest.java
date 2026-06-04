package com.smartlogix.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.mapper.ExternalCarrierMapper;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.service.ExternalCarrierService;
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

@WebMvcTest(ExternalCarrierController.class)
@Import(SecurityConfig.class)
class ExternalCarrierControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ExternalCarrierService carrierService;
    @MockBean ExternalCarrierMapper carrierMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getCarriersByCompanyId_returns200WithList() throws Exception {
        ExternalCarrier carrier = buildCarrier("ec-1");
        ExternalCarrierDTO dto = buildCarrierDTO("ec-1");
        when(carrierService.getCarriersByCompanyId(COMPANY_ID)).thenReturn(List.of(carrier));
        when(carrierMapper.toDto(carrier)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/users/carriers/company/" + COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("ec-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void createCarrier_validRequest_returns201() throws Exception {
        ExternalCarrierDTO dto = buildCarrierDTO(null);
        ExternalCarrier entity = buildCarrier(null);
        ExternalCarrier created = buildCarrier("ec-new");
        ExternalCarrierDTO responseDto = buildCarrierDTO("ec-new");

        when(carrierMapper.toEntity(any())).thenReturn(entity);
        when(carrierService.createCarrier(eq(COMPANY_ID), any())).thenReturn(created);
        when(carrierMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/users/carriers/company/" + COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("ec-new"));
    }

    private ExternalCarrier buildCarrier(String id) {
        return ExternalCarrier.builder()
                .id(id)
                .name("DHL Express")
                .build();
    }

    private ExternalCarrierDTO buildCarrierDTO(String id) {
        ExternalCarrierDTO dto = new ExternalCarrierDTO();
        dto.setId(id);
        dto.setName("DHL Express");
        return dto;
    }
}
