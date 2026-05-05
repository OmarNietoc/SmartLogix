package com.smartlogix.notification.dto;

import com.smartlogix.notification.model.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String orderId,
        String recipient,
        String subject,
        String message,
        NotificationStatus status,
        LocalDateTime createdAt
) {
}
