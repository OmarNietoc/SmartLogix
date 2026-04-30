package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    @Mapping(source = "company.id", target = "companyId")
    UserProfileDTO toDto(UserProfile entity);

    @Mapping(source = "companyId", target = "company.id")
    UserProfile toEntity(UserProfileDTO dto);
}
