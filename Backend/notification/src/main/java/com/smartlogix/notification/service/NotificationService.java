package com.smartlogix.notification.service;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.exception.ResourceNotFoundException;
import com.smartlogix.notification.model.Notification;
import com.smartlogix.notification.model.NotificationStatus;
import com.smartlogix.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .orderId(request.orderId())
                .recipient(request.recipient())
                .subject(request.subject())
                .message(request.message())
                .status(NotificationStatus.ENVIADA)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        System.out.println("==========================================");
        System.out.println("NOTIFICACIÓN ENVIADA");
        System.out.println("Para: " + savedNotification.getRecipient());
        System.out.println("Asunto: " + savedNotification.getSubject());
        System.out.println("Mensaje: " + savedNotification.getMessage());
        System.out.println("Pedido asociado: " + savedNotification.getOrderId());
        System.out.println("==========================================");

        return mapToResponse(savedNotification);
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        return mapToResponse(notification);
    }

    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {
        return notificationRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrderId(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}