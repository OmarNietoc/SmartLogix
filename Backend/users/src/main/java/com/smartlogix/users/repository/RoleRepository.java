package com.smartlogix.users.repository;

import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(RoleName name);
}
