package com.smartlogix.notification.controller;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.MessageResponse;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.service.NotificationService;
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

@Tag(name = "Notifications", description = "Alertas in-app y correos transaccionales. Las notificaciones automáticas se generan vía RabbitMQ desde ms-order")
@RestController
@RequestMapping("/smartlogix/notification/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Crear notificación manual", description = "Crea una notificación de forma manual (sin pasar por el flujo de eventos)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Notificación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                MessageResponse.<NotificationResponse>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Notificación creada exitosamente")
                        .data(notificationService.createNotification(request))
                        .build());
    }

    @Operation(summary = "Listar notificaciones", description = "Retorna todas las notificaciones registradas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getAllNotifications() {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de notificaciones obtenido exitosamente")
                .data(notificationService.getAllNotifications())
                .build());
    }

    @Operation(summary = "Listar notificaciones no leídas", description = "Retorna las notificaciones in-app que aún no han sido marcadas como leídas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    })
    @GetMapping("/unread")
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getUnread() {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificaciones no leídas")
                .data(notificationService.getUnreadInApp())
                .build());
    }

    @Operation(summary = "Marcar notificación como leída", description = "Marca una notificación in-app como leída")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación marcada como leída"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "UUID de la notificación") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<NotificationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificación marcada como leída")
                .data(notificationService.markAsRead(id))
                .build());
    }

    @Operation(summary = "Obtener notificación por ID", description = "Retorna una notificación por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<NotificationResponse>> getNotificationById(
            @Parameter(description = "UUID de la notificación") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<NotificationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificación obtenida exitosamente")
                .data(notificationService.getNotificationById(id))
                .build());
    }

    @Operation(summary = "Notificaciones de un pedido", description = "Retorna todas las notificaciones asociadas a un pedido específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getNotificationsByOrderId(
            @Parameter(description = "UUID del pedido") @PathVariable String orderId) {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificaciones del pedido obtenidas exitosamente")
                .data(notificationService.getNotificationsByOrderId(orderId))
                .build());
    }
}
