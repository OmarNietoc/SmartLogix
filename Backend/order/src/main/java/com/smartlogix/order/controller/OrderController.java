package com.smartlogix.order.controller;

import com.smartlogix.order.dto.*;
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
@RequestMapping("/smartlogix/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Crear pedido", description = "Crea un nuevo pedido y publica el evento OrderCreated hacia RabbitMQ para iniciar el flujo Saga")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Comuna no encontrada")
    })
    @PostMapping("/orders")
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
    @GetMapping("/orders")
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
    @GetMapping("/orders/{id}")
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
    @PutMapping("/orders/{id}/status")
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

    @Operation(summary = "Listar regiones", description = "Retorna todas las regiones de Chile para poblar selectores en el frontend")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Regiones obtenidas exitosamente")
    })
    @GetMapping("/regiones")
    public ResponseEntity<MessageResponse<List<RegionResponse>>> getAllRegiones() {
        List<RegionResponse> regiones = orderService.getAllRegiones();
        return ResponseEntity.ok(
                MessageResponse.<List<RegionResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Regiones obtenidas exitosamente")
                        .data(regiones)
                        .build());
    }

    @Operation(summary = "Listar comunas por región", description = "Retorna las comunas de una región específica para poblar selectores en el frontend")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comunas obtenidas exitosamente")
    })
    @GetMapping("/comunas")
    public ResponseEntity<MessageResponse<List<ComunaResponse>>> getComunasByRegion(
            @Parameter(description = "ID de la región") @RequestParam Integer regionId) {
        List<ComunaResponse> comunas = orderService.getComunasByRegion(regionId);
        return ResponseEntity.ok(
                MessageResponse.<List<ComunaResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Comunas obtenidas exitosamente")
                        .data(comunas)
                        .build());
    }
}
