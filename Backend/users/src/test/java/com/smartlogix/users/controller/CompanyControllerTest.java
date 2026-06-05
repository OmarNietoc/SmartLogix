package com.smartlogix.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.exception.ResourceNotFoundException;
import com.smartlogix.users.mapper.CompanyMapper;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.service.CompanyService;
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

@WebMvcTest(CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CompanyService companyService;
    @MockBean CompanyMapper companyMapper;

    @Test
    void getAllCompanies_returns200WithList() throws Exception {
        Company company = buildCompany("c1");
        CompanyDTO dto = buildCompanyDTO("c1");
        when(companyService.getAllCompanies()).thenReturn(List.of(company));
        when(companyMapper.toDto(company)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/users/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("c1"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void createCompany_validRequest_returns201() throws Exception {
        CompanyDTO dto = buildCompanyDTO(null);
        Company entity = buildCompany(null);
        Company created = buildCompany("c-new");
        CompanyDTO responseDto = buildCompanyDTO("c-new");

        when(companyMapper.toEntity(any())).thenReturn(entity);
        when(companyService.createCompany(entity)).thenReturn(created);
        when(companyMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/users/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("c-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void getAllCompanies_unexpectedException_returns500() throws Exception {
        when(companyService.getAllCompanies()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/smartlogix/users/companies"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCompany_resourceNotFound_returns404() throws Exception {
        CompanyDTO dto = buildCompanyDTO(null);
        when(companyMapper.toEntity(any())).thenReturn(buildCompany(null));
        when(companyService.createCompany(any())).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(post("/smartlogix/users/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCompany_illegalArgument_returns400() throws Exception {
        CompanyDTO dto = buildCompanyDTO(null);
        when(companyMapper.toEntity(any())).thenReturn(buildCompany(null));
        when(companyService.createCompany(any())).thenThrow(new IllegalArgumentException("RUT inválido"));

        mockMvc.perform(post("/smartlogix/users/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    private Company buildCompany(String id) {
        return Company.builder()
                .id(id)
                .name("Empresa Test")
                .taxId("761234560")
                .contactEmail("contacto@empresa.cl")
                .build();
    }

    private CompanyDTO buildCompanyDTO(String id) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(id);
        dto.setName("Empresa Test");
        dto.setTaxId("761234560");
        dto.setContactEmail("contacto@empresa.cl");
        return dto;
    }
}
