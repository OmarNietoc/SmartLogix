package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.mapper.UserProfileMapper;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/users/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<UserProfileDTO>> createProfile(@PathVariable String companyId, @RequestBody UserProfileDTO dto) {
        UserProfile profile = userProfileService.createUserProfile(companyId, userProfileMapper.toEntity(dto));
        MessageResponse<UserProfileDTO> response = MessageResponse.<UserProfileDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User profile created successfully")
                .data(userProfileMapper.toDto(profile))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<UserProfileDTO>>> getProfilesByCompanyId(@PathVariable String companyId) {
        List<UserProfileDTO> profiles = userProfileService.getProfilesByCompanyId(companyId).stream()
                .map(userProfileMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<UserProfileDTO>> response = MessageResponse.<List<UserProfileDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Profiles retrieved successfully")
                .data(profiles)
                .build();
        return ResponseEntity.ok(response);
    }
}
