package com.smartlogix.users.service;

import com.smartlogix.users.model.Company;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Company createCompany(Company company) {
        if (companyRepository.existsByTaxId(company.getTaxId())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el RUT: " + company.getTaxId());
        }
        return companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(String id) {
        return companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));
    }
}
