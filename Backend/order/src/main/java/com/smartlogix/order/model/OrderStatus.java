package com.smartlogix.order.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    REJECTED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == REJECTED || next == CANCELLED;
            case CONFIRMED  -> next == SHIPPED   || next == CANCELLED;
            case SHIPPED    -> next == DELIVERED  || next == CANCELLED;
            case DELIVERED, REJECTED, CANCELLED -> false;
        };
    }
}
