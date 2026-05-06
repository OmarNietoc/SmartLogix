package com.smartlogix.notification.repository;

import com.smartlogix.notification.model.Notification;
import com.smartlogix.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByOrderId(String orderId);
    List<Notification> findByTypeAndReadFalse(NotificationType type);
}
