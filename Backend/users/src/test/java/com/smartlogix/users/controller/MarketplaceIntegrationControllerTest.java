package com.smartlogix.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.mapper.MarketplaceIntegrationMapper;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.service.MarketplaceIntegrationService;
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

@WebMvcTest(MarketplaceIntegrationController.class)
@Import(SecurityConfig.class)
class MarketplaceIntegrationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean MarketplaceIntegrationService integrationService;
    @MockBean MarketplaceIntegrationMapper integrationMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void getIntegrationsByCompanyId_returns200WithList() throws Exception {
        MarketplaceIntegration integration = buildIntegration("mi-1");
        MarketplaceIntegrationDTO dto = buildIntegrationDTO("mi-1");
        when(integrationService.getIntegrationsByCompanyId(COMPANY_ID)).thenReturn(List.of(integration));
        when(integrationMapper.toDto(integration)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/users/integrations/company/" + COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("mi-1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void createIntegration_validRequest_returns201() throws Exception {
        MarketplaceIntegrationDTO dto = buildIntegrationDTO(null);
        MarketplaceIntegration entity = buildIntegration(null);
        MarketplaceIntegration created = buildIntegration("mi-new");
        MarketplaceIntegrationDTO responseDto = buildIntegrationDTO("mi-new");

        when(integrationMapper.toEntity(any())).thenReturn(entity);
        when(integrationService.createIntegration(eq(COMPANY_ID), any())).thenReturn(created);
        when(integrationMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/users/integrations/company/" + COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("mi-new"));
    }

    private MarketplaceIntegration buildIntegration(String id) {
        return MarketplaceIntegration.builder()
                .id(id)
                .platformName("MercadoLibre")
                .build();
    }

    private MarketplaceIntegrationDTO buildIntegrationDTO(String id) {
        MarketplaceIntegrationDTO dto = new MarketplaceIntegrationDTO();
        dto.setId(id);
        dto.setPlatformName("MercadoLibre");
        return dto;
    }
}
