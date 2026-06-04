package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.OrderDeliveredEvent;
import com.smartlogix.notification.service.EmailService;
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
class OrderDeliveredListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private OrderDeliveredListener listener;

    @Test
    @DisplayName("handleOrderDelivered sends delivery email")
    void handleOrderDelivered_validEmail_sendsEmail() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "TRK-001", "juan@email.com");

        listener.handleOrderDelivered(event);

        verify(emailService).sendHtmlEmail(
                eq("juan@email.com"),
                eq("Tu pedido fue entregado — SmartLogix"),
                eq("order-delivered"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderDelivered skips when customerEmail is null")
    void handleOrderDelivered_nullEmail_doesNotSend() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "TRK-001", null);

        listener.handleOrderDelivered(event);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("handleOrderDelivered wraps email exception as AMQP reject")
    void handleOrderDelivered_emailThrows_wrapsAsAmqpException() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "TRK-001", "juan@email.com");
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendHtmlEmail(any(), any(), any(), any());

        assertThatThrownBy(() -> listener.handleOrderDelivered(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
