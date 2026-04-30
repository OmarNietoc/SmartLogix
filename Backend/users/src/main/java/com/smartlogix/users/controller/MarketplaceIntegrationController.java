package com.smartlogix.users.controller;

import com.smartlogix.users.dto.MarketplaceIntegrationDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.MarketplaceIntegrationMapper;
import com.smartlogix.users.model.MarketplaceIntegration;
import com.smartlogix.users.service.MarketplaceIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/users/integrations")
@RequiredArgsConstructor
public class MarketplaceIntegrationController {

    private final MarketplaceIntegrationService integrationService;
    private final MarketplaceIntegrationMapper integrationMapper;

    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<MarketplaceIntegrationDTO>> createIntegration(@PathVariable String companyId, @RequestBody MarketplaceIntegrationDTO dto) {
        MarketplaceIntegration integration = integrationService.createIntegration(companyId, integrationMapper.toEntity(dto));
        MessageResponse<MarketplaceIntegrationDTO> response = MessageResponse.<MarketplaceIntegrationDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Integration created successfully")
                .data(integrationMapper.toDto(integration))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<MarketplaceIntegrationDTO>>> getIntegrationsByCompanyId(@PathVariable String companyId) {
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
