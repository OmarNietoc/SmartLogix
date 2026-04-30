package com.smartlogix.inventario.dto;

import java.util.List;

public record ReservationResponse(
        String orderId,
        String status,
        String message,
        List<String> reservationIds
) {}
