package com.smartlogix.auth_service.service;
import com.smartlogix.auth_service.client.UsersClient;
import com.smartlogix.auth_service.dto.*;
import com.smartlogix.auth_service.model.UserCredential;
import com.smartlogix.auth_service.repository.UserCredentialRepository;
import com.smartlogix.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // 1. Create Company in ms-users
        CompanyDTO companyDto = CompanyDTO.builder()
                .taxId(request.getTaxId())
                .name(request.getCompanyName())
                .contactEmail(request.getContactEmail())
                .phone(request.getPhone())
                .build();
        MessageResponse<CompanyDTO> companyRes = usersClient.createCompany(companyDto);
        String companyId = companyRes.getData().getId();

        // 2. Create UserProfile in ms-users
        UserProfileDTO profileDto = UserProfileDTO.builder()
                .authId(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        usersClient.createAdminProfile(companyId, profileDto);

        // 3. Create UserCredential in ms-auth
        UserCredential credential = UserCredential.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .companyId(companyId)
                .roles(Set.of("ADMIN"))
                .build();
        repository.save(credential);

        String token = jwtUtil.generateToken(request.getEmail(), companyId);
        return new AuthResponse(token, request.getEmail(), companyId);
    }

    public AuthResponse login(LoginRequest request) {
        UserCredential credential = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        
        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        
        String token = jwtUtil.generateToken(request.getEmail(), credential.getCompanyId());
        return new AuthResponse(token, request.getEmail(), credential.getCompanyId());
    }
}
