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
        NotificationResponse created = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                MessageResponse.<NotificationResponse>builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Notificación creada exitosamente")
                        .data(created)
                        .build());
    }

    @GetMapping
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(
                MessageResponse.<List<NotificationResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Listado de notificaciones obtenido exitosamente")
                        .data(notifications)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<NotificationResponse>> getNotificationById(@PathVariable String id) {
        NotificationResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(
                MessageResponse.<NotificationResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Notificación obtenida exitosamente")
                        .data(notification)
                        .build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<MessageResponse<List<NotificationResponse>>> getNotificationsByOrderId(
            @PathVariable String orderId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByOrderId(orderId);
        return ResponseEntity.ok(
                MessageResponse.<List<NotificationResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Notificaciones del pedido obtenidas exitosamente")
                        .data(notifications)
                        .build());
    }
}
