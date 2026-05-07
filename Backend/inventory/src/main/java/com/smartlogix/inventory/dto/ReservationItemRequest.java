package com.smartlogix.inventory.dto;

public record ReservationItemRequest(
        String productId,
        String sku,
        String warehouseId,
        Integer quantity
) {}
