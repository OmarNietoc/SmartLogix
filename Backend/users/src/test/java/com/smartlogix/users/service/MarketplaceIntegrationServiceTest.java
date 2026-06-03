package com.smartlogix.users.service;

import com.smartlogix.users.exception.ResourceNotFoundException;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.MarketplaceIntegrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceIntegrationServiceTest {

    @Mock MarketplaceIntegrationRepository integrationRepository;
    @Mock CompanyRepository companyRepository;
    @InjectMocks MarketplaceIntegrationService marketplaceIntegrationService;

    @Test
    void createIntegration_existingCompany_savesIntegration() {
        // arrange
        Company company = Company.builder().id("company-1").name("Empresa Test").build();
        MarketplaceIntegration integration = MarketplaceIntegration.builder().platformName("Mercado Libre").build();
        MarketplaceIntegration saved = MarketplaceIntegration.builder().id("int-1").platformName("Mercado Libre").company(company).build();

        when(companyRepository.findById("company-1")).thenReturn(Optional.of(company));
        when(integrationRepository.save(integration)).thenReturn(saved);

        // act
        MarketplaceIntegration result = marketplaceIntegrationService.createIntegration("company-1", integration);

        // assert
        assertThat(result.getId()).isEqualTo("int-1");
        assertThat(result.getPlatformName()).isEqualTo("Mercado Libre");
        assertThat(result.getCompany()).isEqualTo(company);
    }

    @Test
    void createIntegration_companyNotFound_throwsResourceNotFoundException() {
        // arrange
        when(companyRepository.findById("missing")).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> marketplaceIntegrationService.createIntegration("missing", new MarketplaceIntegration()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
        verifyNoInteractions(integrationRepository);
    }

    @Test
    void getIntegrationsByCompanyId_returnsIntegrationList() {
        // arrange
        MarketplaceIntegration integration = MarketplaceIntegration.builder().id("int-1").platformName("Shopify").isActive(true).build();
        when(integrationRepository.findByCompanyId("company-1")).thenReturn(List.of(integration));

        // act
        List<MarketplaceIntegration> result = marketplaceIntegrationService.getIntegrationsByCompanyId("company-1");

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlatformName()).isEqualTo("Shopify");
    }

    @Test
    void getIntegrationsByCompanyId_noIntegrations_returnsEmptyList() {
        // arrange
        when(integrationRepository.findByCompanyId("company-2")).thenReturn(List.of());

        // act
        List<MarketplaceIntegration> result = marketplaceIntegrationService.getIntegrationsByCompanyId("company-2");

        // assert
        assertThat(result).isEmpty();
    }
}
