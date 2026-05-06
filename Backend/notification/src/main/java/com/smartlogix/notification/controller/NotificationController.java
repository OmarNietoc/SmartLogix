package com.smartlogix.notification.controller;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.MessageResponse;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/smartlogix/notification/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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

    @GetMapping
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getAllNotifications() {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Listado de notificaciones obtenido exitosamente")
                .data(notificationService.getAllNotifications())
                .build());
    }

    @GetMapping("/unread")
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getUnread() {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificaciones no leídas")
                .data(notificationService.getUnreadInApp())
                .build());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageResponse<NotificationResponse>> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<NotificationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificación marcada como leída")
                .data(notificationService.markAsRead(id))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<NotificationResponse>> getNotificationById(@PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<NotificationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificación obtenida exitosamente")
                .data(notificationService.getNotificationById(id))
                .build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getNotificationsByOrderId(
            @PathVariable String orderId) {
        return ResponseEntity.ok(MessageResponse.<List<NotificationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Notificaciones del pedido obtenidas exitosamente")
                .data(notificationService.getNotificationsByOrderId(orderId))
                .build());
    }
}
