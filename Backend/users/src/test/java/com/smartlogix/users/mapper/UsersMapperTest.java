package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UsersMapperTest {

    private final CompanyMapper companyMapper = new CompanyMapperImpl();
    private final UserProfileMapper userProfileMapper = new UserProfileMapperImpl();
    private final ExternalCarrierMapper externalCarrierMapper = new ExternalCarrierMapperImpl();
    private final MarketplaceIntegrationMapper integrationMapper = new MarketplaceIntegrationMapperImpl();

    @Test
    void companyMapper_mapsEntityAndDtoWithoutCollections() {
        Company entity = Company.builder()
                .id("company-1")
                .taxId("761234560")
                .name("Empresa Demo")
                .contactEmail("contacto@empresa.cl")
                .phone("123456789")
                .build();

        CompanyDTO dto = companyMapper.toDto(entity);
        Company mappedBack = companyMapper.toEntity(dto);

        assertThat(dto.getId()).isEqualTo("company-1");
        assertThat(dto.getName()).isEqualTo("Empresa Demo");
        assertThat(mappedBack.getTaxId()).isEqualTo("761234560");
        assertThat(mappedBack.getUsers()).isNull();
        assertThat(mappedBack.getCarriers()).isNull();
        assertThat(mappedBack.getIntegrations()).isNull();
    }

    @Test
    void userProfileMapper_mapsCompanyIdAndRoles() {
        Company company = Company.builder().id("company-1").build();
        UserProfile entity = UserProfile.builder()
                .id("profile-1")
                .authId("auth-1")
                .company(company)
                .firstName("Ana")
                .lastName("Perez")
                .roles(Set.of(Role.builder().id("role-1").name(RoleName.ADMIN).build()))
                .build();

        UserProfileDTO dto = userProfileMapper.toDto(entity);
        UserProfile mappedBack = userProfileMapper.toEntity(dto);

        assertThat(dto.getCompanyId()).isEqualTo("company-1");
        assertThat(dto.getRoles()).containsExactly("ADMIN");
        assertThat(mappedBack.getCompany().getId()).isEqualTo("company-1");
        assertThat(mappedBack.getRoles()).isEmpty();
    }

    @Test
    void userProfileMapper_returnsEmptyRolesWhenSourceRolesAreNull() {
        UserProfile entity = UserProfile.builder()
                .id("profile-1")
                .authId("auth-1")
                .company(Company.builder().id("company-1").build())
                .firstName("Ana")
                .lastName("Perez")
                .roles(null)
                .build();

        UserProfileDTO dto = userProfileMapper.toDto(entity);

        assertThat(dto.getRoles()).isEmpty();
    }

    @Test
    void externalCarrierMapper_mapsNestedCompanyIdBothDirections() {
        ExternalCarrier entity = ExternalCarrier.builder()
                .id("carrier-1")
                .company(Company.builder().id("company-1").build())
                .name("Carrier Demo")
                .contactEmail("carrier@empresa.cl")
                .phone("123456789")
                .build();

        ExternalCarrierDTO dto = externalCarrierMapper.toDto(entity);
        ExternalCarrier mappedBack = externalCarrierMapper.toEntity(dto);

        assertThat(dto.getCompanyId()).isEqualTo("company-1");
        assertThat(dto.getName()).isEqualTo("Carrier Demo");
        assertThat(mappedBack.getCompany().getId()).isEqualTo("company-1");
    }

    @Test
    void marketplaceIntegrationMapper_mapsNestedCompanyIdAndActiveFlag() {
        MarketplaceIntegration entity = MarketplaceIntegration.builder()
                .id("integration-1")
                .company(Company.builder().id("company-1").build())
                .platformName("Shopify")
                .webhookSecret("secret")
                .isActive(true)
                .build();

        MarketplaceIntegrationDTO dto = integrationMapper.toDto(entity);
        MarketplaceIntegration mappedBack = integrationMapper.toEntity(dto);

        assertThat(dto.getCompanyId()).isEqualTo("company-1");
        assertThat(dto.isActive()).isTrue();
        assertThat(mappedBack.getCompany().getId()).isEqualTo("company-1");
        assertThat(mappedBack.isActive()).isTrue();
    }

    @Test
    void mappersReturnNullWhenInputIsNull() {
        assertThat(companyMapper.toDto(null)).isNull();
        assertThat(companyMapper.toEntity(null)).isNull();
        assertThat(userProfileMapper.toDto(null)).isNull();
        assertThat(userProfileMapper.toEntity(null)).isNull();
        assertThat(externalCarrierMapper.toDto(null)).isNull();
        assertThat(externalCarrierMapper.toEntity(null)).isNull();
        assertThat(integrationMapper.toDto(null)).isNull();
        assertThat(integrationMapper.toEntity(null)).isNull();
    }
}
