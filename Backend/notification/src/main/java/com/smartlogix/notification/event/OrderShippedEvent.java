package com.smartlogix.notification.event;

public record OrderShippedEvent(String orderId, String customerEmail, String trackingNumber) {}
