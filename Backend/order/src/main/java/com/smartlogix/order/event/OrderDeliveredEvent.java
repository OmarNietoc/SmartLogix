package com.smartlogix.order.event;

public record OrderDeliveredEvent(String orderId, String trackingNumber, String customerEmail) {}
