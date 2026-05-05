package com.smartlogix.shipping.event;

public record OrderShippedEvent(String orderId, String customerEmail, String trackingNumber) {}
