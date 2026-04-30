package com.smartlogix.users.service;

import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.repository.CompanyRepository;
import com.smartlogix.users.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;

    public UserProfile createUserProfile(String companyId, UserProfile userProfile) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        userProfile.setCompany(company);
        return userProfileRepository.save(userProfile);
    }

    public List<UserProfile> getProfilesByCompanyId(String companyId) {
        return userProfileRepository.findByCompanyId(companyId);
    }
}
