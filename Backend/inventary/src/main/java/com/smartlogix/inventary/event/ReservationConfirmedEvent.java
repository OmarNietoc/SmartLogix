package com.smartlogix.inventary.event;

public record ReservationConfirmedEvent(String orderId, String customerEmail, String customerName, String shippingAddress) {}
