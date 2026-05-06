package com.smartlogix.order.event;

public record ReservationConfirmedEvent(String orderId, String customerEmail, String customerName) {}
