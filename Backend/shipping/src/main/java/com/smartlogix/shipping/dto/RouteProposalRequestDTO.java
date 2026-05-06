package com.smartlogix.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RouteProposalRequestDTO(
        @NotBlank String originAddress,
        @NotEmpty List<String> shipmentIds
) {}
