package com.smartlogix.notification.service;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.exception.ResourceNotFoundException;
import com.smartlogix.notification.model.Notification;
import com.smartlogix.notification.model.NotificationStatus;
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
                .status(NotificationStatus.ENVIADA)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notificación enviada a {} para orderId={}", saved.getRecipient(), saved.getOrderId());

        return mapToResponse(saved);
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
                n.getSubject(), n.getMessage(), n.getStatus(), n.getCreatedAt());
    }
}
