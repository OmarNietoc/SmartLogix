package com.smartlogix.notification.service;

import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.dto.NotificationResponse;
import com.smartlogix.notification.exception.ResourceNotFoundException;
import com.smartlogix.notification.model.Notification;
import com.smartlogix.notification.model.NotificationStatus;
import com.smartlogix.notification.model.NotificationType;
import com.smartlogix.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks private NotificationService notificationService;

    // ── createNotification ────────────────────────────────────────────────────

    @Test
    @DisplayName("createNotification saves with SENT status and EMAIL type")
    void createNotification_happyPath_savedWithCorrectDefaults() {
        CreateNotificationRequest req = new CreateNotificationRequest(
                "order-1", "user@email.com", "Pedido creado", "Tu pedido esta listo");

        Notification saved = buildNotification("n1", "order-1", NotificationType.EMAIL, false);
        when(notificationRepository.save(any())).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(req);

        assertThat(response.id()).isEqualTo("n1");
        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.type()).isEqualTo(NotificationType.EMAIL);
        assertThat(response.read()).isFalse();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.EMAIL);
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead sets read=true and saves")
    void markAsRead_happyPath_setsReadTrue() {
        Notification notification = buildNotification("n1", "order-1", NotificationType.EMAIL, false);
        when(notificationRepository.findById("n1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead("n1");

        assertThat(response.read()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead throws ResourceNotFoundException when not found")
    void markAsRead_notFound_throwsException() {
        when(notificationRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead("bad"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("bad");
    }

    // ── getAllNotifications ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllNotifications returns mapped list")
    void getAllNotifications_returnsAll() {
        when(notificationRepository.findAll()).thenReturn(List.of(
                buildNotification("n1", "o1", NotificationType.EMAIL, false),
                buildNotification("n2", "o2", NotificationType.IN_APP, true)
        ));

        List<NotificationResponse> result = notificationService.getAllNotifications();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("n1");
        assertThat(result.get(1).read()).isTrue();
    }

    // ── getNotificationById ───────────────────────────────────────────────────

    @Test
    @DisplayName("getNotificationById returns notification when found")
    void getNotificationById_found_returnsResponse() {
        Notification n = buildNotification("n1", "order-1", NotificationType.EMAIL, false);
        when(notificationRepository.findById("n1")).thenReturn(Optional.of(n));

        NotificationResponse response = notificationService.getNotificationById("n1");

        assertThat(response.orderId()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("getNotificationById throws when not found")
    void getNotificationById_notFound_throws() {
        when(notificationRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getNotificationsByOrderId ─────────────────────────────────────────────

    @Test
    @DisplayName("getNotificationsByOrderId returns all for given order")
    void getNotificationsByOrderId_returnsFiltered() {
        when(notificationRepository.findByOrderId("order-1")).thenReturn(List.of(
                buildNotification("n1", "order-1", NotificationType.EMAIL, false),
                buildNotification("n2", "order-1", NotificationType.IN_APP, false)
        ));

        List<NotificationResponse> result = notificationService.getNotificationsByOrderId("order-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> "order-1".equals(r.orderId()));
    }

    // ── getUnreadInApp ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUnreadInApp returns only unread IN_APP notifications")
    void getUnreadInApp_returnsOnlyUnreadInApp() {
        when(notificationRepository.findByTypeAndReadFalse(NotificationType.IN_APP))
                .thenReturn(List.of(buildNotification("n3", "o3", NotificationType.IN_APP, false)));

        List<NotificationResponse> result = notificationService.getUnreadInApp();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.IN_APP);
        assertThat(result.get(0).read()).isFalse();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Notification buildNotification(String id, String orderId, NotificationType type, boolean read) {
        return Notification.builder()
                .id(id)
                .orderId(orderId)
                .recipient("user@email.com")
                .subject("Test subject")
                .message("Test message")
                .status(NotificationStatus.SENT)
                .type(type)
                .read(read)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
