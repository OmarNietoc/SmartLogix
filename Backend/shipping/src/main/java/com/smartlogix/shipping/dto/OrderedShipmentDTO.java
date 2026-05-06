package com.smartlogix.shipping.dto;

import java.math.BigDecimal;

public record OrderedShipmentDTO(
        String shipmentId,
        String shippingAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        int order
) {}
