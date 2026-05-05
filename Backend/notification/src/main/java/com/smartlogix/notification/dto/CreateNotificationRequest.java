package com.smartlogix.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateNotificationRequest(
        String orderId,
        @Email @NotBlank String recipient,
        @NotBlank String subject,
        @NotBlank String message
) {
}
