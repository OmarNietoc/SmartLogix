package com.smartlogix.users.controller;

import com.smartlogix.users.dto.CompanyDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.CompanyMapper;
import com.smartlogix.users.model.Company;
import com.smartlogix.users.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/users/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

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
