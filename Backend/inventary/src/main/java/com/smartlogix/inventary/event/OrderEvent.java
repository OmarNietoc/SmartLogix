package com.smartlogix.inventary.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String customerEmail;
    private String customerName;
    private String status;
    private String subject;
    private String message;
    private LocalDateTime eventDate;
    private List<OrderItemEvent> items;
}
