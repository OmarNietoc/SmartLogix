package com.smartlogix.users.service;

import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock RoleRepository roleRepository;
    @InjectMocks RoleService roleService;

    @Test
    void getAllRoles_returnsAllRoles() {
        // arrange
        Role admin = Role.builder().id("role-admin").name(RoleName.ADMIN).build();
        Role user = Role.builder().id("role-user").name(RoleName.OPERATOR).build();
        when(roleRepository.findAll()).thenReturn(List.of(admin, user));

        // act
        List<Role> result = roleService.getAllRoles();

        // assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    void findByName_existingRole_returnsRole() {
        // arrange
        Role role = Role.builder().id("role-admin").name(RoleName.ADMIN).build();
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(role));

        // act
        Role result = roleService.findByName(RoleName.ADMIN);

        // assert
        assertThat(result.getName()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    void findByName_missingRole_throwsRuntimeException() {
        // arrange
        when(roleRepository.findByName(RoleName.OPERATOR)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> roleService.findByName(RoleName.OPERATOR))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("OPERATOR");
    }

    @Test
    void getAllRoles_emptyRepository_returnsEmptyList() {
        // arrange
        when(roleRepository.findAll()).thenReturn(List.of());

        // act
        List<Role> result = roleService.getAllRoles();

        // assert
        assertThat(result).isEmpty();
    }
}
