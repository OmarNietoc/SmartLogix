package com.smartlogix.auth_service.service;

import com.smartlogix.auth_service.client.UsersClient;
import com.smartlogix.auth_service.dto.*;
import com.smartlogix.auth_service.model.UserCredential;
import com.smartlogix.auth_service.repository.UserCredentialRepository;
import com.smartlogix.auth_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserCredentialRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UsersClient usersClient;
    @InjectMocks private AuthService authService;

    @Test
    void register_normalizesEmailCreatesCompanyProfileAndCredential() {
        RegisterRequest request = new RegisterRequest();
        request.setCompanyName(" Logistica Andina ");
        request.setTaxId("76.123.456-0");
        request.setFirstName(" Ana ");
        request.setLastName(" Perez ");
        request.setEmail("ADMIN@SMARTLOGIX.CL ");
        request.setPassword("secret123");
        request.setPhone("123456789");

        CompanyDTO company = CompanyDTO.builder().id("company-1").build();
        MessageResponse<CompanyDTO> companyResponse = new MessageResponse<>(201, "ok", company);
        when(repository.existsByEmail("admin@smartlogix.cl")).thenReturn(false);
        when(usersClient.createCompany(any(CompanyDTO.class))).thenReturn(companyResponse);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(jwtUtil.generateToken("admin@smartlogix.cl", "company-1")).thenReturn("jwt");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt");
        assertThat(response.getEmail()).isEqualTo("admin@smartlogix.cl");
        assertThat(response.getCompanyId()).isEqualTo("company-1");

        ArgumentCaptor<UserCredential> credentialCaptor = ArgumentCaptor.forClass(UserCredential.class);
        verify(repository).save(credentialCaptor.capture());
        assertThat(credentialCaptor.getValue().getEmail()).isEqualTo("admin@smartlogix.cl");
        assertThat(credentialCaptor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(credentialCaptor.getValue().getRoles()).containsExactly("ADMIN");
        verify(usersClient).createAdminProfile(eq("company-1"), any(UserProfileDTO.class));
    }

    @Test
    void register_rejectsDuplicateEmailBeforeCallingUsersService() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@smartlogix.cl");

        when(repository.existsByEmail("admin@smartlogix.cl")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registrado");
        verifyNoInteractions(usersClient);
    }

    @Test
    void login_validCredentialsReturnsToken() {
        UserCredential credential = UserCredential.builder()
                .email("admin@smartlogix.cl")
                .password("encoded")
                .companyId("company-1")
                .build();
        LoginRequest request = new LoginRequest();
        request.setEmail("ADMIN@SMARTLOGIX.CL ");
        request.setPassword("secret123");
        when(repository.findByEmail("admin@smartlogix.cl")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("admin@smartlogix.cl", "company-1")).thenReturn("jwt");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt");
        assertThat(response.getCompanyId()).isEqualTo("company-1");
    }

    @Test
    void login_rejectsMissingUserOrBadPassword() {
        LoginRequest missing = new LoginRequest();
        missing.setEmail("missing@smartlogix.cl");
        missing.setPassword("secret123");
        when(repository.findByEmail("missing@smartlogix.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(missing))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales");

        LoginRequest badPassword = new LoginRequest();
        badPassword.setEmail("admin@smartlogix.cl");
        badPassword.setPassword("wrong");
        UserCredential credential = UserCredential.builder().email("admin@smartlogix.cl").password("encoded").build();
        when(repository.findByEmail("admin@smartlogix.cl")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(badPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales");
    }
}
