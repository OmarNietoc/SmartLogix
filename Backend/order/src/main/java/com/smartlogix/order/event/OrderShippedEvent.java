package com.smartlogix.order.event;

public record OrderShippedEvent(String orderId, String customerEmail, String trackingNumber) {}
