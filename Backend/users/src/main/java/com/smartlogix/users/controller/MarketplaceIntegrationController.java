package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.MarketplaceIntegrationMapper;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.service.MarketplaceIntegrationService;
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

@Tag(name = "Users - Integraciones", description = "Integraciones de marketplace vinculadas a una empresa (MercadoLibre, Shopify, etc.)")
@RestController
@RequestMapping("/smartlogix/users/integrations")
@RequiredArgsConstructor
public class MarketplaceIntegrationController {

    private final MarketplaceIntegrationService integrationService;
    private final MarketplaceIntegrationMapper integrationMapper;

    @Operation(summary = "Crear integración marketplace", description = "Registra una nueva integración de marketplace para una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Integración creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<MarketplaceIntegrationDTO>> createIntegration(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId,
            @RequestBody MarketplaceIntegrationDTO dto) {
        MarketplaceIntegration integration = integrationService.createIntegration(companyId, integrationMapper.toEntity(dto));
        MessageResponse<MarketplaceIntegrationDTO> response = MessageResponse.<MarketplaceIntegrationDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Integration created successfully")
                .data(integrationMapper.toDto(integration))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar integraciones de empresa", description = "Retorna todas las integraciones marketplace registradas para una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<MarketplaceIntegrationDTO>>> getIntegrationsByCompanyId(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId) {
        List<MarketplaceIntegrationDTO> integrations = integrationService.getIntegrationsByCompanyId(companyId).stream()
                .map(integrationMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<MarketplaceIntegrationDTO>> response = MessageResponse.<List<MarketplaceIntegrationDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Integrations retrieved successfully")
                .data(integrations)
                .build();
        return ResponseEntity.ok(response);
    }
}
