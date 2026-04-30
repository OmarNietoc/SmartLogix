package com.smartlogix.order.service;

import com.smartlogix.order.client.NotificationClient;
import com.smartlogix.order.dto.*;
import com.smartlogix.order.exception.ResourceNotFoundException;
import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderItem;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final NotificationClient notificationClient;

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setShippingAddress(request.shippingAddress());
        order.setStatus(OrderStatus.PENDIENTE);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(itemRequest.price());
            item.setOrder(order);

            order.getItems().add(item);

            BigDecimal subtotal = itemRequest.price()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));
            total = total.add(subtotal);
        }

        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);

        notificationClient.sendNotification(
                new CreateNotificationRequest(
                        savedOrder.getId(),
                        savedOrder.getCustomerEmail(),
                        "Pedido creado",
                        "Tu pedido con ID " + savedOrder.getId() + " fue creado correctamente y quedó en estado PENDIENTE."
                )
        );

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return mapToResponse(order);
    }

    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        notificationClient.sendNotification(
                new CreateNotificationRequest(
                        updatedOrder.getId(),
                        updatedOrder.getCustomerEmail(),
                        "Actualización de pedido",
                        "Tu pedido con ID " + updatedOrder.getId() + " cambió a estado " + updatedOrder.getStatus() + "."
                )
        );

        return mapToResponse(updatedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }
}