package com.smartlogix.order.dto;

public record CreateNotificationRequest(
        Long orderId,
        String recipient,
        String subject,
        String message
) {
}