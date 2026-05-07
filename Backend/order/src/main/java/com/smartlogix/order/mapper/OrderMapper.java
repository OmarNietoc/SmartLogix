package com.smartlogix.order.mapper;

import com.smartlogix.order.dto.OrderItemResponse;
import com.smartlogix.order.dto.OrderResponse;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "comuna.id",            target = "comunaId")
    @Mapping(source = "comuna.nombre",        target = "comunaNombre")
    @Mapping(source = "comuna.region.nombre", target = "regionNombre")
    OrderResponse toDto(Order order);

    OrderItemResponse itemToDto(OrderItem item);
}
