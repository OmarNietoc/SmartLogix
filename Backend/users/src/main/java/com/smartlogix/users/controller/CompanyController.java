package com.smartlogix.users.controller;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.CompanyMapper;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users - Empresas", description = "Registro y consulta de PYMEs en SmartLogix")
@RestController
@RequestMapping("/smartlogix/users/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

    @Operation(summary = "Registrar empresa", description = "Crea una nueva empresa (PYME) en el sistema SmartLogix")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<CompanyDTO>> createCompany(@RequestBody CompanyDTO dto) {
        Company company = companyService.createCompany(companyMapper.toEntity(dto));
        MessageResponse<CompanyDTO> response = MessageResponse.<CompanyDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Company created successfully")
                .data(companyMapper.toDto(company))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar empresas", description = "Retorna todas las empresas registradas en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<CompanyDTO>>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.getAllCompanies().stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
        MessageResponse<List<CompanyDTO>> response = MessageResponse.<List<CompanyDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Companies retrieved successfully")
                .data(companies)
                .build();
        return ResponseEntity.ok(response);
    }
}
