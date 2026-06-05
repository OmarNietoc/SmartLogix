package com.smartlogix.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.exception.ResourceNotFoundException;
import com.smartlogix.users.mapper.CompanyMapperImpl;
import com.smartlogix.users.mapper.ExternalCarrierMapperImpl;
import com.smartlogix.users.mapper.MarketplaceIntegrationMapperImpl;
import com.smartlogix.users.mapper.UserProfileMapperImpl;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.service.CompanyService;
import com.smartlogix.users.service.ExternalCarrierService;
import com.smartlogix.users.service.MarketplaceIntegrationService;
import com.smartlogix.users.service.RoleService;
import com.smartlogix.users.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        CompanyController.class,
        UserProfileController.class,
        RoleController.class,
        ExternalCarrierController.class,
        MarketplaceIntegrationController.class
})
@Import({
        SecurityConfig.class,
        CompanyMapperImpl.class,
        UserProfileMapperImpl.class,
        ExternalCarrierMapperImpl.class,
        MarketplaceIntegrationMapperImpl.class
})
class UsersControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CompanyService companyService;
    @MockBean private UserProfileService userProfileService;
    @MockBean private RoleService roleService;
    @MockBean private ExternalCarrierService carrierService;
    @MockBean private MarketplaceIntegrationService integrationService;

    @Test
    void createCompany_validRequest_returns201() throws Exception {
        Company company = company();
        when(companyService.createCompany(any())).thenReturn(company);

        mockMvc.perform(post("/smartlogix/users/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").value("company-1"))
                .andExpect(jsonPath("$.data.taxId").value("761234560"));
    }

    @Test
    void createCompany_invalidRut_returns400() throws Exception {
        CompanyDTO dto = companyDto();
        dto.setTaxId("bad-rut");
        when(companyService.createCompany(any()))
                .thenThrow(new IllegalArgumentException("El RUT ingresado no es valido"));

        mockMvc.perform(post("/smartlogix/users/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("RUT")));
    }

    @Test
    void listCompanies_returns200() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(company()));

        mockMvc.perform(get("/smartlogix/users/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Empresa Demo"));
    }

    @Test
    void createAdminProfile_companyNotFound_returns404() throws Exception {
        when(userProfileService.createAdminProfile(eq("missing"), any()))
                .thenThrow(new ResourceNotFoundException("Empresa no encontrada"));

        mockMvc.perform(post("/smartlogix/users/profiles/company/missing/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Empresa no encontrada"));
    }

    @Test
    void createProfile_usesCompanyHeaderAndReturns201() throws Exception {
        when(userProfileService.createUserProfile(eq("company-1"), any(), eq(Set.of(RoleName.OPERATOR))))
                .thenReturn(profile());

        mockMvc.perform(post("/smartlogix/users/profiles/company")
                        .header("X-Company-Id", "company-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("profile-1"));
    }

    @Test
    void assignRoles_invalidRole_returns400() throws Exception {
        mockMvc.perform(put("/smartlogix/users/profiles/profile-1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"NOT_A_ROLE\"]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listProfiles_returns200() throws Exception {
        when(userProfileService.getProfilesByCompanyId("company-1")).thenReturn(List.of(profile()));

        mockMvc.perform(get("/smartlogix/users/profiles/company")
                        .header("X-Company-Id", "company-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].authId").value("auth-1"));
    }

    @Test
    void listRoles_returnsRoleCatalog() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().id("role-1").name(RoleName.ADMIN).build()));

        mockMvc.perform(get("/smartlogix/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("ADMIN"));
    }

    @Test
    void createAndListExternalCarriers_returnExpectedContracts() throws Exception {
        when(carrierService.createCarrier(eq("company-1"), any())).thenReturn(carrier());
        when(carrierService.getCarriersByCompanyId("company-1")).thenReturn(List.of(carrier()));

        mockMvc.perform(post("/smartlogix/users/carriers/company/company-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrierDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Carrier Demo"));

        mockMvc.perform(get("/smartlogix/users/carriers/company/company-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].companyId").value("company-1"));
    }

    @Test
    void createAndListMarketplaceIntegrations_returnExpectedContracts() throws Exception {
        when(integrationService.createIntegration(eq("company-1"), any())).thenReturn(integration());
        when(integrationService.getIntegrationsByCompanyId("company-1")).thenReturn(List.of(integration()));

        mockMvc.perform(post("/smartlogix/users/integrations/company/company-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(integrationDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.platformName").value("Shopify"))
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(get("/smartlogix/users/integrations/company/company-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].companyId").value("company-1"));
    }

    private Company company() {
        return Company.builder()
                .id("company-1")
                .taxId("761234560")
                .name("Empresa Demo")
                .contactEmail("contacto@empresa.cl")
                .phone("123456789")
                .build();
    }

    private CompanyDTO companyDto() {
        return CompanyDTO.builder()
                .taxId("761234560")
                .name("Empresa Demo")
                .contactEmail("contacto@empresa.cl")
                .phone("123456789")
                .build();
    }

    private UserProfile profile() {
        return UserProfile.builder()
                .id("profile-1")
                .authId("auth-1")
                .company(Company.builder().id("company-1").build())
                .firstName("Ana")
                .lastName("Perez")
                .roles(Set.of(Role.builder().id("role-1").name(RoleName.OPERATOR).build()))
                .build();
    }

    private UserProfileDTO profileDto() {
        return UserProfileDTO.builder()
                .authId("auth-1")
                .firstName("Ana")
                .lastName("Perez")
                .roles(Set.of("OPERATOR"))
                .build();
    }

    private ExternalCarrier carrier() {
        return ExternalCarrier.builder()
                .id("carrier-1")
                .company(Company.builder().id("company-1").build())
                .name("Carrier Demo")
                .contactEmail("carrier@empresa.cl")
                .phone("123456789")
                .build();
    }

    private ExternalCarrierDTO carrierDto() {
        return ExternalCarrierDTO.builder()
                .name("Carrier Demo")
                .contactEmail("carrier@empresa.cl")
                .phone("123456789")
                .build();
    }

    private MarketplaceIntegration integration() {
        return MarketplaceIntegration.builder()
                .id("integration-1")
                .company(Company.builder().id("company-1").build())
                .platformName("Shopify")
                .webhookSecret("secret")
                .isActive(true)
                .build();
    }

    private MarketplaceIntegrationDTO integrationDto() {
        return MarketplaceIntegrationDTO.builder()
                .platformName("Shopify")
                .webhookSecret("secret")
                .isActive(true)
                .build();
    }
}
