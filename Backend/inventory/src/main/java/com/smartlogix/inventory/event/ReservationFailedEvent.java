package com.smartlogix.inventory.event;

public record ReservationFailedEvent(String orderId, String productId, String reason, String customerEmail, String customerName) {}
