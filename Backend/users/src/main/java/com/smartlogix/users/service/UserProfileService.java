package com.smartlogix.users.service;

import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.RoleRepository;
import com.smartlogix.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartlogix.users.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    public UserProfile createAdminProfile(String companyId, UserProfile userProfile) {
        return createUserProfile(companyId, userProfile, Set.of(RoleName.ADMIN));
    }

    public UserProfile createUserProfile(String companyId, UserProfile userProfile, Set<RoleName> roleNames) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + companyId));
        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new ResourceNotFoundException("Uno o más roles no encontrados");
        }
        if (userProfileRepository.existsByAuthId(userProfile.getAuthId())) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo electrónico");
        }
        userProfile.setCompany(company);
        userProfile.setRoles(roles);
        return userProfileRepository.save(userProfile);
    }

    public UserProfile assignRolesToProfile(String profileId, Set<RoleName> roleNames) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de usuario no encontrado con id: " + profileId));
        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new ResourceNotFoundException("Uno o más roles no encontrados");
        }
        profile.setRoles(roles);
        return userProfileRepository.save(profile);
    }

    public List<UserProfile> getProfilesByCompanyId(String companyId) {
        return userProfileRepository.findByCompanyId(companyId);
    }
}
