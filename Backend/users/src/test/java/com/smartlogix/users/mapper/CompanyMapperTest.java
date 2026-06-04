package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.mapper.CompanyMapperImpl;
import com.smartlogix.users.model.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CompanyMapperTest {

    private final CompanyMapper mapper = new CompanyMapperImpl();

    @Test
    @DisplayName("toDto maps all fields from Company to CompanyDTO")
    void toDto_mapsAllFields() {
        Company company = Company.builder()
                .id("c1")
                .taxId("76.123.456-0")
                .name("Empresa Test")
                .contactEmail("contacto@empresa.cl")
                .phone("+56912345678")
                .build();

        CompanyDTO dto = mapper.toDto(company);

        assertThat(dto.getId()).isEqualTo("c1");
        assertThat(dto.getTaxId()).isEqualTo("76.123.456-0");
        assertThat(dto.getName()).isEqualTo("Empresa Test");
        assertThat(dto.getContactEmail()).isEqualTo("contacto@empresa.cl");
        assertThat(dto.getPhone()).isEqualTo("+56912345678");
    }

    @Test
    @DisplayName("toDto returns null when company is null")
    void toDto_null_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("toEntity maps all non-ignored fields from CompanyDTO to Company")
    void toEntity_mapsFields() {
        CompanyDTO dto = CompanyDTO.builder()
                .id("c1")
                .taxId("761234560")
                .name("Empresa Test")
                .contactEmail("contacto@empresa.cl")
                .phone("+56912345678")
                .build();

        Company company = mapper.toEntity(dto);

        assertThat(company.getId()).isEqualTo("c1");
        assertThat(company.getTaxId()).isEqualTo("761234560");
        assertThat(company.getName()).isEqualTo("Empresa Test");
        assertThat(company.getContactEmail()).isEqualTo("contacto@empresa.cl");
    }

    @Test
    @DisplayName("toEntity returns null when dto is null")
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
