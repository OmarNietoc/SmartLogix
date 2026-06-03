package com.smartlogix.users.service;

import com.smartlogix.users.exception.ResourceNotFoundException;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.ExternalCarrierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalCarrierServiceTest {

    @Mock ExternalCarrierRepository carrierRepository;
    @Mock CompanyRepository companyRepository;
    @InjectMocks ExternalCarrierService externalCarrierService;

    @Test
    void createCarrier_existingCompany_savesCarrier() {
        // arrange
        Company company = Company.builder().id("company-1").name("Empresa Test").build();
        ExternalCarrier carrier = ExternalCarrier.builder().name("DHL Express").build();
        ExternalCarrier saved = ExternalCarrier.builder().id("carrier-1").name("DHL Express").company(company).build();

        when(companyRepository.findById("company-1")).thenReturn(Optional.of(company));
        when(carrierRepository.save(carrier)).thenReturn(saved);

        // act
        ExternalCarrier result = externalCarrierService.createCarrier("company-1", carrier);

        // assert
        assertThat(result.getId()).isEqualTo("carrier-1");
        assertThat(result.getCompany()).isEqualTo(company);
        verify(carrierRepository).save(carrier);
    }

    @Test
    void createCarrier_companyNotFound_throwsResourceNotFoundException() {
        // arrange
        when(companyRepository.findById("missing")).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> externalCarrierService.createCarrier("missing", new ExternalCarrier()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
        verifyNoInteractions(carrierRepository);
    }

    @Test
    void getCarriersByCompanyId_returnsCarrierList() {
        // arrange
        ExternalCarrier carrier = ExternalCarrier.builder().id("carrier-1").name("Starken").build();
        when(carrierRepository.findByCompanyId("company-1")).thenReturn(List.of(carrier));

        // act
        List<ExternalCarrier> result = externalCarrierService.getCarriersByCompanyId("company-1");

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Starken");
    }

    @Test
    void getCarriersByCompanyId_noCarriers_returnsEmptyList() {
        // arrange
        when(carrierRepository.findByCompanyId("company-2")).thenReturn(List.of());

        // act
        List<ExternalCarrier> result = externalCarrierService.getCarriersByCompanyId("company-2");

        // assert
        assertThat(result).isEmpty();
    }
}
