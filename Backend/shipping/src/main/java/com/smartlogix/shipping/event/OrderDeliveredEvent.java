package com.smartlogix.shipping.event;

public record OrderDeliveredEvent(String orderId, String trackingNumber, String customerEmail) {}
