package com.smartlogix.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String shippingAddress;
    private String status;
    private String subject;
    private String message;
    private LocalDateTime eventDate;
    private List<OrderItemEvent> items;
}
