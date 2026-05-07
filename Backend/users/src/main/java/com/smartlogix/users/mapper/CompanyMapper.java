package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDTO toDto(Company entity);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "integrations", ignore = true)
    @Mapping(target = "carriers", ignore = true)
    Company toEntity(CompanyDTO dto);
}
