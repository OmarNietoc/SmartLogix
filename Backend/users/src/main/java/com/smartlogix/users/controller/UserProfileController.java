package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.mapper.UserProfileMapper;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users - Perfiles", description = "Gestión de perfiles de usuario vinculados a una empresa")
@RestController
@RequestMapping("/smartlogix/users/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    @Operation(summary = "Crear perfil de usuario", description = "Registra un nuevo perfil de usuario asociado a una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<UserProfileDTO>> createProfile(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId,
            @RequestBody UserProfileDTO dto) {
        UserProfile profile = userProfileService.createUserProfile(companyId, userProfileMapper.toEntity(dto));
        MessageResponse<UserProfileDTO> response = MessageResponse.<UserProfileDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User profile created successfully")
                .data(userProfileMapper.toDto(profile))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar perfiles de empresa", description = "Retorna todos los perfiles de usuario registrados para una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<UserProfileDTO>>> getProfilesByCompanyId(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId) {
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
