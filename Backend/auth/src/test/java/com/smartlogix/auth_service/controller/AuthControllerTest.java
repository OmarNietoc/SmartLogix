package com.smartlogix.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.auth_service.dto.AuthResponse;
import com.smartlogix.auth_service.dto.LoginRequest;
import com.smartlogix.auth_service.dto.RegisterRequest;
import com.smartlogix.auth_service.security.CustomUserDetailsService;
import com.smartlogix.auth_service.security.SecurityConfig;
import com.smartlogix.auth_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@smartlogix.cl");
        request.setPassword("secret123");
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", "admin@smartlogix.cl", "company-1"));

        // act & assert
        mockMvc.perform(post("/smartlogix/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@smartlogix.cl"))
                .andExpect(jsonPath("$.companyId").value("company-1"));
    }

    @Test
    void login_badCredentials_returns400() throws Exception {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@smartlogix.cl");
        request.setPassword("wrong");
        when(authService.login(any())).thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        // act & assert
        mockMvc.perform(post("/smartlogix/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        // arrange
        RegisterRequest request = buildValidRegisterRequest();
        when(authService.register(any())).thenReturn(new AuthResponse("jwt-token", "nuevo@empresa.cl", "company-2"));

        // act & assert
        mockMvc.perform(post("/smartlogix/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.companyId").value("company-2"));
    }

    @Test
    void register_invalidEmail_returns400WithoutCallingService() throws Exception {
        // arrange
        RegisterRequest request = buildValidRegisterRequest();
        request.setEmail("not-valid-email");

        // act & assert
        mockMvc.perform(post("/smartlogix/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        // arrange
        RegisterRequest request = buildValidRegisterRequest();
        request.setPassword("short");

        // act & assert
        mockMvc.perform(post("/smartlogix/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private RegisterRequest buildValidRegisterRequest() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("nuevo@empresa.cl");
        r.setPassword("password123");
        r.setCompanyName("Mi Empresa");
        r.setTaxId("76.123.456-0");
        r.setFirstName("Juan");
        r.setLastName("Pérez");
        return r;
    }
}
