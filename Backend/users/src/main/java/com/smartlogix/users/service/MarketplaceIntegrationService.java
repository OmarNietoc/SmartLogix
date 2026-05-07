package com.smartlogix.users.service;

import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.MarketplaceIntegrationRepository;
import com.smartlogix.users.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketplaceIntegrationService {

    private final MarketplaceIntegrationRepository integrationRepository;
    private final CompanyRepository companyRepository;

    public MarketplaceIntegration createIntegration(String companyId, MarketplaceIntegration integration) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + companyId));
        integration.setCompany(company);
        return integrationRepository.save(integration);
    }

    public List<MarketplaceIntegration> getIntegrationsByCompanyId(String companyId) {
        return integrationRepository.findByCompanyId(companyId);
    }
}
