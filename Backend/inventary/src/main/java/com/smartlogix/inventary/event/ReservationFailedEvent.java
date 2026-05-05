package com.smartlogix.inventary.event;

public record ReservationFailedEvent(String orderId, String productId, String reason) {}
