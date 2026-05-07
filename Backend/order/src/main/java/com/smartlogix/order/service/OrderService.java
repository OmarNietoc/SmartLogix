package com.smartlogix.order.service;

import com.smartlogix.order.config.RabbitMQConfig;
import com.smartlogix.order.dto.*;
import com.smartlogix.order.event.OrderEvent;
import com.smartlogix.order.event.OrderItemEvent;
import com.smartlogix.order.exception.ResourceNotFoundException;
import com.smartlogix.order.mapper.OrderMapper;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrderMapper orderMapper;

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        String shippingAddress = request.street() + ", " + request.commune() + ", "
                + request.city() + ", " + request.region() + ", Chile";
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setWarehouseId(itemRequest.warehouseId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(itemRequest.price());
            item.setOrder(order);
            order.getItems().add(item);
            total = total.add(itemRequest.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        List<OrderItemEvent> itemEvents = savedOrder.getItems().stream()
                .map(i -> OrderItemEvent.builder()
                        .productId(i.getProductId())
                        .warehouseId(i.getWarehouseId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        OrderEvent event = OrderEvent.builder()
                .orderId(savedOrder.getId())
                .customerEmail(savedOrder.getCustomerEmail())
                .customerName(savedOrder.getCustomerName())
                .shippingAddress(savedOrder.getShippingAddress())
                .status(savedOrder.getStatus().name())
                .subject("Pedido creado")
                .message("Tu pedido con ID " + savedOrder.getId() + " fue creado correctamente y quedó en estado PENDIENTE.")
                .eventDate(LocalDateTime.now())
                .items(itemEvents)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "order.created", event);

        return orderMapper.toDto(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toDto).toList();
    }

    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return orderMapper.toDto(order);
    }

    public OrderResponse updateOrderStatus(String id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        if (!order.getStatus().canTransitionTo(request.status())) {
            throw new IllegalStateException("Transición inválida: " + order.getStatus() + " → " + request.status());
        }
        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        OrderEvent event = OrderEvent.builder()
                .orderId(updatedOrder.getId())
                .customerEmail(updatedOrder.getCustomerEmail())
                .customerName(updatedOrder.getCustomerName())
                .status(updatedOrder.getStatus().name())
                .subject("Actualización de pedido")
                .message("Tu pedido con ID " + updatedOrder.getId() + " cambió a estado " + updatedOrder.getStatus() + ".")
                .eventDate(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "order.updated", event);

        return orderMapper.toDto(updatedOrder);
    }
}
