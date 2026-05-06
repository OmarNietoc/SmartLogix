package com.smartlogix.notification.event;

public record OrderDeliveredEvent(String orderId, String trackingNumber, String customerEmail) {}
