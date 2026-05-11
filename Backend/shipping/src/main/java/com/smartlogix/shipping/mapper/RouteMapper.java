package com.smartlogix.shipping.mapper;

import com.smartlogix.shipping.dto.RouteDTO;
import com.smartlogix.shipping.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ShipmentMapper.class})
public interface RouteMapper {

    RouteDTO toDto(Route route);

    @Mapping(target = "shipments", ignore = true)
    Route toEntity(RouteDTO dto);
}
