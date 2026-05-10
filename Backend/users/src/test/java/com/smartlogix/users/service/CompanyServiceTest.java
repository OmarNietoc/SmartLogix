package com.smartlogix.users.service;

import com.smartlogix.users.model.Company;
import com.smartlogix.users.repository.CompanyRepository;
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
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;

    @InjectMocks private CompanyService companyService;

    // ── createCompany ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createCompany saves and returns company")
    void createCompany_savesAndReturns() {
        Company company = buildCompany("c1");
        when(companyRepository.save(company)).thenReturn(company);

        Company result = companyService.createCompany(company);

        assertThat(result.getId()).isEqualTo("c1");
        assertThat(result.getName()).isEqualTo("Empresa c1");
        assertThat(result.getTaxId()).isEqualTo("761234560");
        verify(companyRepository).save(company);
    }

    @Test
    @DisplayName("createCompany rejects invalid RUT")
    void createCompany_invalidRut_throws() {
        Company company = buildCompany("c1");
        company.setTaxId("76.123.456-8");

        assertThatThrownBy(() -> companyService.createCompany(company))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RUT");
    }

    @Test
    @DisplayName("createCompany rejects duplicate normalized RUT")
    void createCompany_duplicateRut_throws() {
        Company company = buildCompany("c1");
        when(companyRepository.existsByNormalizedTaxId("761234560")).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(company))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }

    // ── getAllCompanies ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCompanies returns all companies")
    void getAllCompanies_returnsAll() {
        when(companyRepository.findAll()).thenReturn(
                List.of(buildCompany("c1"), buildCompany("c2"), buildCompany("c3")));

        List<Company> result = companyService.getAllCompanies();

        assertThat(result).hasSize(3);
        verify(companyRepository).findAll();
    }

    @Test
    @DisplayName("getAllCompanies returns empty list when no companies")
    void getAllCompanies_empty_returnsEmptyList() {
        when(companyRepository.findAll()).thenReturn(List.of());

        List<Company> result = companyService.getAllCompanies();

        assertThat(result).isEmpty();
    }

    // ── getCompanyById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCompanyById found returns company")
    void getCompanyById_found_returnsCompany() {
        Company company = buildCompany("c1");
        when(companyRepository.findById("c1")).thenReturn(Optional.of(company));

        Company result = companyService.getCompanyById("c1");

        assertThat(result.getId()).isEqualTo("c1");
        assertThat(result.getTaxId()).isEqualTo("76.123.456-0");
    }

    @Test
    @DisplayName("getCompanyById not found throws RuntimeException")
    void getCompanyById_notFound_throws() {
        when(companyRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById("bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Empresa no encontrada");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Company buildCompany(String id) {
        return Company.builder()
                .id(id)
                .taxId("76.123.456-0")
                .name("Empresa " + id)
                .contactEmail("contacto@empresa.cl")
                .phone("+56912345678")
                .build();
    }
}
