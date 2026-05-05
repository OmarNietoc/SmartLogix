package com.smartlogix.order.controller;

import com.smartlogix.order.dto.CreateOrderRequest;
import com.smartlogix.order.dto.MessageResponse;
import com.smartlogix.order.dto.OrderResponse;
import com.smartlogix.order.dto.UpdateOrderStatusRequest;
import com.smartlogix.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/smartlogix/order/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<MessageResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                MessageResponse.<OrderResponse>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Pedido creado exitosamente")
                        .data(created)
                        .build());
    }

    @GetMapping
    public ResponseEntity<MessageResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
                MessageResponse.<List<OrderResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Listado de pedidos obtenido exitosamente")
                        .data(orders)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(
                MessageResponse.<OrderResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Pedido obtenido exitosamente")
                        .data(order)
                        .build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MessageResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(
                MessageResponse.<OrderResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Estado del pedido actualizado exitosamente")
                        .data(updated)
                        .build());
    }
}
