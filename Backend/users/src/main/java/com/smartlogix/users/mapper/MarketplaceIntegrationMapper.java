package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.model.MarketplaceIntegration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MarketplaceIntegrationMapper {
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "active", target = "isActive")
    MarketplaceIntegrationDTO toDto(MarketplaceIntegration entity);

    @Mapping(source = "companyId", target = "company.id")
    @Mapping(source = "active", target = "isActive")
    MarketplaceIntegration toEntity(MarketplaceIntegrationDTO dto);
}
