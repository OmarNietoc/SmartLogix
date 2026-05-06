package com.smartlogix.order.event;

public record ReservationFailedEvent(String orderId, String productId, String reason, String customerEmail, String customerName) {}
