package com.smartlogix.order.controller;

import com.smartlogix.order.dto.CreateOrderRequest;
import com.smartlogix.order.dto.MessageResponse;
import com.smartlogix.order.dto.OrderResponse;
import com.smartlogix.order.dto.UpdateOrderStatusRequest;
import com.smartlogix.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Creación y seguimiento de pedidos de clientes")
@RestController
@RequestMapping("/smartlogix/order/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido y publica el evento OrderCreated hacia RabbitMQ para iniciar el flujo Saga")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
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

    @Operation(summary = "Listar pedidos", description = "Retorna todos los pedidos registrados en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    @Operation(summary = "Obtener pedido por ID", description = "Retorna un pedido por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<OrderResponse>> getOrderById(
            @Parameter(description = "UUID del pedido") @PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(
                MessageResponse.<OrderResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Pedido obtenido exitosamente")
                        .data(order)
                        .build());
    }

    @Operation(summary = "Actualizar estado del pedido", description = "Cambia el estado de un pedido (PENDING, CONFIRMED, CANCELLED, DELIVERED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<MessageResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "UUID del pedido") @PathVariable String id,
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
