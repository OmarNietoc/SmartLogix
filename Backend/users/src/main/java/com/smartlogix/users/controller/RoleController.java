package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.dto.RoleDTO;
import com.smartlogix.users.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users - Roles", description = "Catálogo de roles del sistema")
@RestController
@RequestMapping("/smartlogix/users/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Listar roles", description = "Retorna el catálogo completo de roles disponibles en el sistema")
    @GetMapping
    public ResponseEntity<MessageResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles().stream()
                .map(r -> new RoleDTO(r.getId(), r.getName().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<RoleDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roles)
                .build());
    }
}
