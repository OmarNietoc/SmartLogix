package com.smartlogix.shipping.strategy;

import com.smartlogix.shipping.model.Route;

public interface ShippingCalculationStrategy {
    String calculateRoute(Route route);
}
