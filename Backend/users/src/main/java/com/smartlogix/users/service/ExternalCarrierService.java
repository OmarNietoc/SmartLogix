package com.smartlogix.users.service;

import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.ExternalCarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalCarrierService {

    private final ExternalCarrierRepository carrierRepository;
    private final CompanyRepository companyRepository;

    public ExternalCarrier createCarrier(String companyId, ExternalCarrier carrier) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        carrier.setCompany(company);
        return carrierRepository.save(carrier);
    }

    public List<ExternalCarrier> getCarriersByCompanyId(String companyId) {
        return carrierRepository.findByCompanyId(companyId);
    }
}
