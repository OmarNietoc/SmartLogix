package com.smartlogix.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String id,
        String productId,
        String warehouseId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
