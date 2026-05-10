package com.smartlogix.auth_service.service;
import com.smartlogix.auth_service.client.UsersClient;
import com.smartlogix.auth_service.dto.*;
import com.smartlogix.auth_service.model.UserCredential;
import com.smartlogix.auth_service.repository.UserCredentialRepository;
import com.smartlogix.auth_service.security.JwtUtil;
import com.smartlogix.auth_service.validation.ChileanRutValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsersClient usersClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String contactEmail = request.getContactEmail() == null || request.getContactEmail().isBlank()
                ? normalizedEmail
                : request.getContactEmail().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        String normalizedTaxId = ChileanRutValidator.normalize(request.getTaxId());

        // 1. Create Company in ms-users
        CompanyDTO companyDto = CompanyDTO.builder()
                .taxId(normalizedTaxId)
                .name(request.getCompanyName().trim())
                .contactEmail(contactEmail)
                .phone(request.getPhone())
                .build();
        MessageResponse<CompanyDTO> companyRes = usersClient.createCompany(companyDto);
        String companyId = companyRes.getData().getId();

        // 2. Create UserProfile in ms-users
        UserProfileDTO profileDto = UserProfileDTO.builder()
                .authId(normalizedEmail)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .build();
        usersClient.createAdminProfile(companyId, profileDto);

        // 3. Create UserCredential in ms-auth
        UserCredential credential = UserCredential.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .companyId(companyId)
                .roles(Set.of("ADMIN"))
                .build();
        repository.save(credential);

        String token = jwtUtil.generateToken(normalizedEmail, companyId);
        return new AuthResponse(token, normalizedEmail, companyId);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        UserCredential credential = repository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        
        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        
        String token = jwtUtil.generateToken(normalizedEmail, credential.getCompanyId());
        return new AuthResponse(token, normalizedEmail, credential.getCompanyId());
    }
}
