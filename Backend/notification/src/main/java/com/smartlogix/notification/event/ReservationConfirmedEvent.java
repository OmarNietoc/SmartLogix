package com.smartlogix.notification.event;

public record ReservationConfirmedEvent(String orderId, String customerEmail, String customerName) {}
