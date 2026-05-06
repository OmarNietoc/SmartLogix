package com.smartlogix.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_id")
    private String orderId;

    private String recipient;

    private String subject;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType type = NotificationType.EMAIL;

    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
