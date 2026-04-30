package com.smartlogix.inventario.dto;

import java.util.List;

public record ReservationRequest(
        String orderId,
        List<ReservationItemRequest> items
) {}
