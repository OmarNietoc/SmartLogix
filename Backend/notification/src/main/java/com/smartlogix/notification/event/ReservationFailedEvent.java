package com.smartlogix.notification.event;

public record ReservationFailedEvent(String orderId, String productId, String reason, String customerEmail, String customerName) {}
