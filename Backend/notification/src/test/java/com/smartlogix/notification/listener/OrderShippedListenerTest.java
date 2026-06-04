package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.OrderShippedEvent;
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
class OrderShippedListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private OrderShippedListener listener;

    @Test
    @DisplayName("handleOrderShipped sends shipping email")
    void handleOrderShipped_validEvent_sendsEmail() {
        OrderShippedEvent event = new OrderShippedEvent("order-1", "juan@email.com", "TRK-002");

        listener.handleOrderShipped(event);

        verify(emailService).sendHtmlEmail(
                eq("juan@email.com"),
                eq("Tu pedido está en camino — SmartLogix"),
                eq("order-shipped"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderShipped wraps email exception as AMQP reject")
    void handleOrderShipped_emailThrows_wrapsAsAmqpException() {
        OrderShippedEvent event = new OrderShippedEvent("order-1", "juan@email.com", "TRK-002");
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendHtmlEmail(any(), any(), any(), any());

        assertThatThrownBy(() -> listener.handleOrderShipped(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
