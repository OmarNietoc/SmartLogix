package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.ReservationConfirmedEvent;
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
class OrderConfirmedListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private OrderConfirmedListener listener;

    @Test
    @DisplayName("handleOrderConfirmed sends confirmation email")
    void handleOrderConfirmed_validEmail_sendsEmail() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent("order-1", "juan@email.com", "Juan", "Calle 1");

        listener.handleOrderConfirmed(event);

        verify(emailService).sendHtmlEmail(
                eq("juan@email.com"),
                eq("Tu pedido fue confirmado — SmartLogix"),
                eq("order-confirmed"),
                any(Context.class)
        );
    }

    @Test
    @DisplayName("handleOrderConfirmed skips when customerEmail is null")
    void handleOrderConfirmed_nullEmail_doesNotSend() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent("order-1", null, "Juan", "Calle 1");

        listener.handleOrderConfirmed(event);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("handleOrderConfirmed wraps email exception as AMQP reject")
    void handleOrderConfirmed_emailThrows_wrapsAsAmqpException() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent("order-1", "juan@email.com", "Juan", "Calle 1");
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendHtmlEmail(any(), any(), any(), any());

        assertThatThrownBy(() -> listener.handleOrderConfirmed(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
