package com.smartlogix.inventory.event;

public record ReservationConfirmedEvent(String orderId, String customerEmail, String customerName, String shippingAddress, String companyId) {}
