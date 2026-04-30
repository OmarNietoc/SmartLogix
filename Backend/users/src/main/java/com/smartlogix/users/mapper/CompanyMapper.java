package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.model.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDTO toDto(Company entity);
    Company toEntity(CompanyDTO dto);
}
