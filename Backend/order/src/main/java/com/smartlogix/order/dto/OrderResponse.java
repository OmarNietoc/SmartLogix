package com.smartlogix.order.dto;

import com.smartlogix.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String customerName,
        String customerEmail,
        String street,
        Integer comunaId,
        String comunaNombre,
        String regionNombre,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {}
