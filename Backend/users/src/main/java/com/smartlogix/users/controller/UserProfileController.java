package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.dto.UserProfileDTO;
import com.smartlogix.users.mapper.UserProfileMapper;
import com.smartlogix.users.model.RoleName;
import com.smartlogix.users.model.UserProfile;
import com.smartlogix.users.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Users - Perfiles", description = "Gestión de perfiles de usuario vinculados a una empresa")
@RestController
@RequestMapping("/smartlogix/users/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    @Operation(summary = "Registrar empresa — crear perfil admin", description = "Crea un perfil de usuario con rol ADMIN automático al registrar una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Perfil admin creado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping("/company/{companyId}/admin")
    public ResponseEntity<MessageResponse<UserProfileDTO>> createAdminProfile(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId,
            @Valid @RequestBody UserProfileDTO dto) {
        UserProfile profile = userProfileService.createAdminProfile(companyId, userProfileMapper.toEntity(dto));
        return new ResponseEntity<>(MessageResponse.<UserProfileDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Admin profile created successfully")
                .data(userProfileMapper.toDto(profile))
                .build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Crear perfil de empleado", description = "El admin crea un perfil de empleado con roles explícitos")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Perfil creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o rol inexistente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<UserProfileDTO>> createProfile(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId,
            @Valid @RequestBody UserProfileDTO dto) {
        Set<RoleName> roleNames = dto.getRoles() == null ? Set.of() :
                dto.getRoles().stream().map(RoleName::valueOf).collect(Collectors.toSet());
        UserProfile profile = userProfileService.createUserProfile(companyId, userProfileMapper.toEntity(dto), roleNames);
        return new ResponseEntity<>(MessageResponse.<UserProfileDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User profile created successfully")
                .data(userProfileMapper.toDto(profile))
                .build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Asignar roles a perfil", description = "Actualiza los roles asignados a un perfil de usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Perfil o rol no encontrado")
    })
    @PutMapping("/{profileId}/roles")
    public ResponseEntity<MessageResponse<UserProfileDTO>> assignRoles(
            @Parameter(description = "UUID del perfil") @PathVariable String profileId,
            @RequestBody Set<String> roleNames) {
        Set<RoleName> roles = roleNames.stream().map(RoleName::valueOf).collect(Collectors.toSet());
        UserProfile profile = userProfileService.assignRolesToProfile(profileId, roles);
        return ResponseEntity.ok(MessageResponse.<UserProfileDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles updated successfully")
                .data(userProfileMapper.toDto(profile))
                .build());
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
        return ResponseEntity.ok(MessageResponse.<List<UserProfileDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Profiles retrieved successfully")
                .data(profiles)
                .build());
    }
}
