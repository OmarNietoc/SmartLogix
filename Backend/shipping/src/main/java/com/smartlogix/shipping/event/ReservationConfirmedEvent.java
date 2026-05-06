package com.smartlogix.shipping.event;

public record ReservationConfirmedEvent(String orderId, String customerEmail, String customerName, String shippingAddress) {}
