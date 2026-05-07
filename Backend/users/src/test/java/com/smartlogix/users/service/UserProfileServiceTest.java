package com.smartlogix.users.service;

import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private CompanyRepository companyRepository;

    @InjectMocks private UserProfileService userProfileService;

    // ── createUserProfile ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createUserProfile happy path links company and saves")
    void createUserProfile_happyPath_linksCompanyAndSaves() {
        Company company = buildCompany("c1");
        UserProfile profile = buildProfile("p1");

        when(companyRepository.findById("c1")).thenReturn(Optional.of(company));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userProfileService.createUserProfile("c1", profile);

        assertThat(result.getCompany()).isEqualTo(company);
        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("createUserProfile throws when company not found")
    void createUserProfile_companyNotFound_throws() {
        when(companyRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.createUserProfile("bad", buildProfile("p1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Company not found");
    }

    // ── getProfilesByCompanyId ────────────────────────────────────────────────

    @Test
    @DisplayName("getProfilesByCompanyId returns profiles for given company")
    void getProfilesByCompanyId_returnsProfiles() {
        when(userProfileRepository.findByCompanyId("c1"))
                .thenReturn(List.of(buildProfile("p1"), buildProfile("p2")));

        List<UserProfile> result = userProfileService.getProfilesByCompanyId("c1");

        assertThat(result).hasSize(2);
        verify(userProfileRepository).findByCompanyId("c1");
    }

    @Test
    @DisplayName("getProfilesByCompanyId returns empty list when no profiles")
    void getProfilesByCompanyId_noProfiles_returnsEmpty() {
        when(userProfileRepository.findByCompanyId("c99")).thenReturn(List.of());

        List<UserProfile> result = userProfileService.getProfilesByCompanyId("c99");

        assertThat(result).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Company buildCompany(String id) {
        return Company.builder()
                .id(id)
                .taxId("76123456-7")
                .name("Empresa " + id)
                .contactEmail("contacto@empresa.cl")
                .build();
    }

    private UserProfile buildProfile(String id) {
        return UserProfile.builder()
                .id(id)
                .authId("auth-" + id)
                .firstName("Nombre")
                .lastName("Apellido")
                .role("ADMIN")
                .build();
    }
}
