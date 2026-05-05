package com.smartlogix.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private String customerEmail;
    private String customerName;
    private String status;
    private String subject;
    private String message;
    private LocalDateTime eventDate;
}
