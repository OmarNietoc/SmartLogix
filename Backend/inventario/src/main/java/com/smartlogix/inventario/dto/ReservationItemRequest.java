package com.smartlogix.inventario.dto;

public record ReservationItemRequest(
        String productId,
        String sku,
        String warehouseId,
        Integer quantity
) {}
