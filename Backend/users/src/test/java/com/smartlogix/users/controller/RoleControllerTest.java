package com.smartlogix.users.controller;

import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.model.Role;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean RoleService roleService;

    @Test
    void getAllRoles_returns200WithList() throws Exception {
        Role adminRole = Role.builder().id("r1").name(RoleName.ADMIN).build();
        Role operatorRole = Role.builder().id("r2").name(RoleName.OPERATOR).build();
        when(roleService.getAllRoles()).thenReturn(List.of(adminRole, operatorRole));

        mockMvc.perform(get("/smartlogix/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("ADMIN"))
                .andExpect(jsonPath("$.data[1].name").value("OPERATOR"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void getAllRoles_emptyList_returns200() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of());

        mockMvc.perform(get("/smartlogix/users/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
