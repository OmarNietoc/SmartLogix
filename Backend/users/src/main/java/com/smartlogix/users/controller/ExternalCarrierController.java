package com.smartlogix.users.controller;

import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.ExternalCarrierMapper;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.service.ExternalCarrierService;
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

@Tag(name = "Users - Transportistas", description = "Gestión de carriers externos vinculados a una empresa")
@RestController
@RequestMapping("/smartlogix/users/carriers")
@RequiredArgsConstructor
public class ExternalCarrierController {

    private final ExternalCarrierService carrierService;
    private final ExternalCarrierMapper carrierMapper;

    @Operation(summary = "Registrar transportista externo", description = "Crea un nuevo carrier externo asociado a una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transportista creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<ExternalCarrierDTO>> createCarrier(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId,
            @RequestBody ExternalCarrierDTO dto) {
        ExternalCarrier carrier = carrierService.createCarrier(companyId, carrierMapper.toEntity(dto));
        MessageResponse<ExternalCarrierDTO> response = MessageResponse.<ExternalCarrierDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Carrier created successfully")
                .data(carrierMapper.toDto(carrier))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar transportistas de empresa", description = "Retorna todos los carriers externos registrados para una empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<ExternalCarrierDTO>>> getCarriersByCompanyId(
            @Parameter(description = "UUID de la empresa") @PathVariable String companyId) {
        List<ExternalCarrierDTO> carriers = carrierService.getCarriersByCompanyId(companyId).stream()
                .map(carrierMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<ExternalCarrierDTO>> response = MessageResponse.<List<ExternalCarrierDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Carriers retrieved successfully")
                .data(carriers)
                .build();
        return ResponseEntity.ok(response);
    }
}
