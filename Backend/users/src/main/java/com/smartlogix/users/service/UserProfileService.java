package com.smartlogix.users.service;

import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.RoleRepository;
import com.smartlogix.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    public UserProfile createAdminProfile(String companyId, UserProfile userProfile) {
        return createUserProfile(companyId, userProfile, Set.of(RoleName.ADMIN));
    }

    public UserProfile createUserProfile(String companyId, UserProfile userProfile, Set<RoleName> roleNames) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Set<Role> roles = roleNames.stream()
                .map(rn -> roleRepository.findByName(rn)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + rn)))
                .collect(Collectors.toSet());
        userProfile.setCompany(company);
        userProfile.setRoles(roles);
        return userProfileRepository.save(userProfile);
    }

    public UserProfile assignRolesToProfile(String profileId, Set<RoleName> roleNames) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found: " + profileId));
        Set<Role> roles = roleNames.stream()
                .map(rn -> roleRepository.findByName(rn)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + rn)))
                .collect(Collectors.toSet());
        profile.setRoles(roles);
        return userProfileRepository.save(profile);
    }

    public List<UserProfile> getProfilesByCompanyId(String companyId) {
        return userProfileRepository.findByCompanyId(companyId);
    }
}
