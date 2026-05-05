package com.smartlogix.inventary.dto;

public record ReservationItemRequest(
        String productId,
        String sku,
        String warehouseId,
        Integer quantity
) {}
