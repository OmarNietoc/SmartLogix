package com.smartlogix.order.mapper;

import com.smartlogix.order.dto.OrderItemResponse;
import com.smartlogix.order.dto.OrderResponse;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toDto(Order order);
    OrderItemResponse itemToDto(OrderItem item);
}
