package com.smartlogix.users.mapper;

import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "roles", target = "roles")
    UserProfileDTO toDto(UserProfile entity);

    @Mapping(source = "companyId", target = "company.id")
    @Mapping(target = "roles", ignore = true)
    UserProfile toEntity(UserProfileDTO dto);

    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) return new HashSet<>();
        return roles.stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}
