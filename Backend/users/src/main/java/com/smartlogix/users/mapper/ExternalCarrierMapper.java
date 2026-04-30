package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.model.ExternalCarrier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExternalCarrierMapper {
    @Mapping(source = "company.id", target = "companyId")
    ExternalCarrierDTO toDto(ExternalCarrier entity);

    @Mapping(source = "companyId", target = "company.id")
    ExternalCarrier toEntity(ExternalCarrierDTO dto);
}
