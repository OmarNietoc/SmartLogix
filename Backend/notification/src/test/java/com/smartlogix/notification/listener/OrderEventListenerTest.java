package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.OrderEvent;
import com.smartlogix.notification.service.EmailService;
import com.smartlogix.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    @InjectMocks private OrderEventListener orderEventListener;

    // ── handleOrderCreated ────────────────────────────────────────────────────

    @Test
    @DisplayName("handleOrderCreated creates notification and sends email")
    void handleOrderCreated_happyPath_createsNotificationAndSendsEmail() {
        OrderEvent event = buildOrderEvent("order-1", "juan@email.com", "Juan", "Pedido creado", "Tu pedido fue creado.");

        orderEventListener.handleOrderCreated(event);

        verify(notificationService).createNotification(argThat(req ->
                req.orderId().equals("order-1") &&
                req.recipient().equals("juan@email.com") &&
                req.subject().equals("Pedido creado")
        ));
        verify(emailService).sendHtmlEmail(
                eq("juan@email.com"),
                eq("Pedido creado"),
                eq("order-created"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderCreated uses default subject when event subject is null")
    void handleOrderCreated_nullSubject_usesDefaultSubject() {
        OrderEvent event = buildOrderEvent("order-2", "ana@email.com", "Ana", null, "Mensaje");

        orderEventListener.handleOrderCreated(event);

        verify(emailService).sendHtmlEmail(
                eq("ana@email.com"),
                eq("Confirmación de pedido — SmartLogix"),
                eq("order-created"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderCreated passes correct variables to Thymeleaf context")
    void handleOrderCreated_setsThymeleafContextVariables() {
        OrderEvent event = buildOrderEvent("order-3", "carlos@email.com", "Carlos", "Asunto", "Mensaje test");

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        orderEventListener.handleOrderCreated(event);

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString(), ctxCaptor.capture());
        Context ctx = ctxCaptor.getValue();
        assertThat(ctx.getVariable("orderId")).isEqualTo("order-3");
        assertThat(ctx.getVariable("customerName")).isEqualTo("Carlos");
        assertThat(ctx.getVariable("message")).isEqualTo("Mensaje test");
    }

    @Test
    @DisplayName("handleOrderCreated still sends email even when notificationService succeeds")
    void handleOrderCreated_alwaysSendsBothNotificationAndEmail() {
        OrderEvent event = buildOrderEvent("order-4", "test@email.com", "Test", "Asunto", "Msg");

        orderEventListener.handleOrderCreated(event);

        verify(notificationService, times(1)).createNotification(any());
        verify(emailService, times(1)).sendHtmlEmail(any(), any(), any(), any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OrderEvent buildOrderEvent(String orderId, String email, String name, String subject, String message) {
        return OrderEvent.builder()
                .orderId(orderId)
                .customerEmail(email)
                .customerName(name)
                .subject(subject)
                .message(message)
                .status("PENDING")
                .eventDate(LocalDateTime.now())
                .build();
    }
}
