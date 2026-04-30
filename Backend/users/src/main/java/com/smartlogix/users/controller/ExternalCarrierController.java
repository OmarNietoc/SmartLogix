package com.smartlogix.users.controller;

import com.smartlogix.users.dto.ExternalCarrierDTO;
import com.smartlogix.users.dto.MessageResponse;
import com.smartlogix.users.mapper.ExternalCarrierMapper;
import com.smartlogix.users.model.ExternalCarrier;
import com.smartlogix.users.service.ExternalCarrierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/smartlogix/users/carriers")
@RequiredArgsConstructor
public class ExternalCarrierController {

    private final ExternalCarrierService carrierService;
    private final ExternalCarrierMapper carrierMapper;

    @PostMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<ExternalCarrierDTO>> createCarrier(@PathVariable String companyId, @RequestBody ExternalCarrierDTO dto) {
        ExternalCarrier carrier = carrierService.createCarrier(companyId, carrierMapper.toEntity(dto));
        MessageResponse<ExternalCarrierDTO> response = MessageResponse.<ExternalCarrierDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Carrier created successfully")
                .data(carrierMapper.toDto(carrier))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<MessageResponse<List<ExternalCarrierDTO>>> getCarriersByCompanyId(@PathVariable String companyId) {
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
