package com.smartlogix.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.users.config.SecurityConfig;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.mapper.UserProfileMapper;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@Import(SecurityConfig.class)
class UserProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserProfileService userProfileService;
    @MockBean UserProfileMapper userProfileMapper;

    private static final String COMPANY_ID = "company-1";

    @Test
    void createAdminProfile_validRequest_returns201() throws Exception {
        UserProfileDTO dto = buildProfileDTO(null);
        UserProfile entity = buildProfile(null);
        UserProfile created = buildProfile("p-new");
        UserProfileDTO responseDto = buildProfileDTO("p-new");

        when(userProfileMapper.toEntity(any())).thenReturn(entity);
        when(userProfileService.createAdminProfile(eq(COMPANY_ID), any())).thenReturn(created);
        when(userProfileMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/users/profiles/company/" + COMPANY_ID + "/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("p-new"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void createProfile_validRequest_returns201() throws Exception {
        UserProfileDTO dto = buildProfileDTO(null);
        UserProfile entity = buildProfile(null);
        UserProfile created = buildProfile("p-new");
        UserProfileDTO responseDto = buildProfileDTO("p-new");

        when(userProfileMapper.toEntity(any())).thenReturn(entity);
        when(userProfileService.createUserProfile(eq(COMPANY_ID), any(), any())).thenReturn(created);
        when(userProfileMapper.toDto(created)).thenReturn(responseDto);

        mockMvc.perform(post("/smartlogix/users/profiles/company")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("p-new"));
    }

    @Test
    void assignRoles_validRequest_returns200() throws Exception {
        UserProfile updated = buildProfile("p1");
        UserProfileDTO responseDto = buildProfileDTO("p1");
        when(userProfileService.assignRolesToProfile(eq("p1"), any())).thenReturn(updated);
        when(userProfileMapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(put("/smartlogix/users/profiles/p1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Set.of("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Roles updated successfully"));
    }

    @Test
    void getProfilesByCompanyId_returns200WithList() throws Exception {
        UserProfile profile = buildProfile("p1");
        UserProfileDTO dto = buildProfileDTO("p1");
        when(userProfileService.getProfilesByCompanyId(COMPANY_ID)).thenReturn(List.of(profile));
        when(userProfileMapper.toDto(profile)).thenReturn(dto);

        mockMvc.perform(get("/smartlogix/users/profiles/company")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("p1"));
    }

    private UserProfile buildProfile(String id) {
        return UserProfile.builder()
                .id(id)
                .authId("auth-" + id)
                .firstName("Ana")
                .lastName("Pérez")
                .build();
    }

    private UserProfileDTO buildProfileDTO(String id) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(id);
        dto.setFirstName("Ana");
        dto.setLastName("Pérez");
        return dto;
    }
}
