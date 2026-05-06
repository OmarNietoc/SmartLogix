package com.smartlogix.notification.service;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.exception.ResourceNotFoundException;
import com.smartlogix.notification.model.Notification;
import com.smartlogix.notification.model.NotificationStatus;
import com.smartlogix.notification.model.NotificationType;
import com.smartlogix.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
                .status(NotificationStatus.SENT)
                .type(NotificationType.EMAIL)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Notificación EMAIL guardada para orderId={}", saved.getOrderId());
        return mapToResponse(saved);
    }

    public NotificationResponse createInAppNotification(String orderId, String recipient, String subject, String message) {
        Notification notification = Notification.builder()
                .orderId(orderId)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.SENT)
                .type(NotificationType.IN_APP)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Notificación IN_APP creada para orderId={}", orderId);
        return mapToResponse(saved);
    }

    public NotificationResponse markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        notification.setRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getUnreadInApp() {
        return notificationRepository.findByTypeAndReadFalse(NotificationType.IN_APP)
                .stream().map(this::mapToResponse).toList();
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public NotificationResponse getNotificationById(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        return mapToResponse(notification);
    }

    public List<NotificationResponse> getNotificationsByOrderId(String orderId) {
        return notificationRepository.findByOrderId(orderId).stream().map(this::mapToResponse).toList();
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getOrderId(), n.getRecipient(),
                n.getSubject(), n.getMessage(), n.getStatus(), n.getType(), n.isRead(), n.getCreatedAt());
    }
}
