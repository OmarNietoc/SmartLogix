package com.smartlogix.inventory.dto;

public record StockRequest(
        String productId,
        String sku,
        String warehouseId,
        Integer quantity,
        String reason
) {}
