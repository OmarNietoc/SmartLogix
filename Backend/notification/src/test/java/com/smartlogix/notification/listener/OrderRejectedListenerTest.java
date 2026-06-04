package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.ReservationFailedEvent;
import com.smartlogix.notification.service.EmailService;
import com.smartlogix.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRejectedListenerTest {

    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @InjectMocks private OrderRejectedListener listener;

    @Test
    @DisplayName("handleOrderRejected creates in-app notification and sends email")
    void handleOrderRejected_withEmail_createsNotificationAndSendsEmail() {
        ReservationFailedEvent event =
                new ReservationFailedEvent("order-1", "prod-1", "stock insuficiente", "juan@email.com", "Juan");

        listener.handleOrderRejected(event);

        verify(notificationService).createInAppNotification(
                eq("order-1"), eq("juan@email.com"), anyString(), anyString());
        verify(emailService).sendHtmlEmail(
                eq("juan@email.com"),
                eq("Tu pedido no pudo ser procesado — SmartLogix"),
                eq("order-rejected"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderRejected skips email when customerEmail is null")
    void handleOrderRejected_nullEmail_onlyCreatesInAppNotification() {
        ReservationFailedEvent event =
                new ReservationFailedEvent("order-1", "prod-1", "reason", null, "Juan");

        listener.handleOrderRejected(event);

        verify(notificationService).createInAppNotification(
                eq("order-1"), eq("SYSTEM"), anyString(), anyString());
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("handleOrderRejected wraps exception as AMQP reject")
    void handleOrderRejected_notificationServiceThrows_wrapsAsAmqpException() {
        ReservationFailedEvent event =
                new ReservationFailedEvent("order-1", "prod-1", "reason", "juan@email.com", "Juan");
        doThrow(new RuntimeException("DB error")).when(notificationService)
                .createInAppNotification(any(), any(), any(), any());

        assertThatThrownBy(() -> listener.handleOrderRejected(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
