package com.smartlogix.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @Email @NotBlank String customerEmail,
        @NotBlank String street,
        @NotNull Integer comunaId,
        @Valid @NotEmpty List<OrderItemRequest> items
) {}
