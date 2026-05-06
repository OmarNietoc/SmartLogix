package com.smartlogix.shipping.dto;

import java.util.List;

public record RouteProposalResponseDTO(
        String source,
        double distanceKm,
        int durationMinutes,
        int totalStops,
        List<OrderedShipmentDTO> orderedShipments
) {}
