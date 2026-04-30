package com.smartlogix.users.repository;

import com.smartlogix.users.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByAuthId(String authId);
    List<UserProfile> findByCompanyId(String companyId);
}
