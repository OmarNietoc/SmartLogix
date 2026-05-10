package com.smartlogix.users.service;

import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.RoleRepository;
import com.smartlogix.users.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private UserProfileService userProfileService;

    // ── createAdminProfile ────────────────────────────────────────────────────

    @Test
    @DisplayName("createAdminProfile assigns ADMIN role automatically")
    void createAdminProfile_assignsAdminRoleAutomatically() {
        Company company = buildCompany("c1");
        UserProfile profile = buildProfile("p1");
        Role adminRole = buildRole(RoleName.ADMIN);

        when(companyRepository.findById("c1")).thenReturn(Optional.of(company));
        when(roleRepository.findByNameIn(Set.of(RoleName.ADMIN))).thenReturn(Set.of(adminRole));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userProfileService.createAdminProfile("c1", profile);

        assertThat(result.getRoles()).containsExactly(adminRole);
        verify(roleRepository).findByNameIn(Set.of(RoleName.ADMIN));
        verify(userProfileRepository).save(profile);
    }

    // ── createUserProfile ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createUserProfile with roles resolves them from repository")
    void createUserProfile_withRoles_resolvesFromRepository() {
        Company company = buildCompany("c1");
        UserProfile profile = buildProfile("p1");
        Role operatorRole = buildRole(RoleName.OPERATOR);

        when(companyRepository.findById("c1")).thenReturn(Optional.of(company));
        when(roleRepository.findByNameIn(Set.of(RoleName.OPERATOR))).thenReturn(Set.of(operatorRole));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userProfileService.createUserProfile("c1", profile, Set.of(RoleName.OPERATOR));

        assertThat(result.getRoles()).containsExactly(operatorRole);
        assertThat(result.getCompany()).isEqualTo(company);
        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("createUserProfile throws when company not found")
    void createUserProfile_companyNotFound_throws() {
        when(companyRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.createUserProfile("bad", buildProfile("p1"), Set.of(RoleName.ADMIN)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Empresa no encontrada");
    }

    @Test
    @DisplayName("createUserProfile throws when role not found in repository")
    void createUserProfile_invalidRole_throwsException() {
        when(companyRepository.findById("c1")).thenReturn(Optional.of(buildCompany("c1")));
        when(roleRepository.findByNameIn(Set.of(RoleName.DRIVER))).thenReturn(Set.of());

        assertThatThrownBy(() -> userProfileService.createUserProfile("c1", buildProfile("p1"), Set.of(RoleName.DRIVER)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("roles no encontrados");
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
                .build();
    }

    private Role buildRole(RoleName roleName) {
        return Role.builder()
                .id("role-" + roleName.name().toLowerCase())
                .name(roleName)
                .build();
    }
}
