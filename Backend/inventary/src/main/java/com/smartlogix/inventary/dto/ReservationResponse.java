package com.smartlogix.inventary.dto;

import java.util.List;

public record ReservationResponse(
        String orderId,
        String status,
        String message,
        List<String> reservationIds
) {}
