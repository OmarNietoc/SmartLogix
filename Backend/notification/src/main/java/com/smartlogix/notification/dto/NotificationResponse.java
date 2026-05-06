package com.smartlogix.notification.dto;

import com.smartlogix.notification.model.NotificationStatus;
import com.smartlogix.notification.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String orderId,
        String recipient,
        String subject,
        String message,
        NotificationStatus status,
        NotificationType type,
        boolean read,
        LocalDateTime createdAt
) {}
