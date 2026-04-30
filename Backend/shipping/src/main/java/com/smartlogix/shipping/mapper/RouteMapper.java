package com.smartlogix.shipping.mapper;

import com.smartlogix.shipping.dto.RouteDTO;
import com.smartlogix.shipping.model.Route;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ShipmentMapper.class})
public interface RouteMapper {

    RouteDTO toDto(Route route);

    Route toEntity(RouteDTO dto);
}
