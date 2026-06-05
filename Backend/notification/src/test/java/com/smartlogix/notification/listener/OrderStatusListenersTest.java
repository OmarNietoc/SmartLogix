package com.smartlogix.notification.listener;

import com.smartlogix.notification.event.OrderDeliveredEvent;
import com.smartlogix.notification.event.OrderShippedEvent;
import com.smartlogix.notification.event.ReservationConfirmedEvent;
import com.smartlogix.notification.event.ReservationFailedEvent;
import com.smartlogix.notification.service.EmailService;
import com.smartlogix.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderStatusListenersTest {

    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    @Test
    void orderConfirmed_sendsConfirmationEmailWithContext() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1",
                "cliente@smartlogix.cl",
                "Cliente Demo",
                "Av. Demo 123"
        );

        new OrderConfirmedListener(emailService).handleOrderConfirmed(event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(emailService).sendHtmlEmail(
                eq("cliente@smartlogix.cl"),
                eq("Tu pedido fue confirmado — SmartLogix"),
                eq("order-confirmed"),
                contextCaptor.capture()
        );
        assertThat(contextCaptor.getValue().getVariable("orderId")).isEqualTo("order-1");
        assertThat(contextCaptor.getValue().getVariable("customerName")).isEqualTo("Cliente Demo");
    }

    @Test
    void orderConfirmed_ignoresEventWithoutCustomerEmail() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent("order-1", null, "Cliente Demo", "Av. Demo");

        new OrderConfirmedListener(emailService).handleOrderConfirmed(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void orderConfirmed_wrapsEmailFailuresAsAmqpReject() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1",
                "cliente@smartlogix.cl",
                "Cliente Demo",
                "Av. Demo 123"
        );
        doThrow(new RuntimeException("smtp down")).when(emailService)
                .sendHtmlEmail(eq("cliente@smartlogix.cl"), any(), eq("order-confirmed"), any(Context.class));

        assertThatThrownBy(() -> new OrderConfirmedListener(emailService).handleOrderConfirmed(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("order-1");
    }

    @Test
    void orderShipped_sendsTrackingEmail() {
        OrderShippedEvent event = new OrderShippedEvent("order-1", "cliente@smartlogix.cl", "SL-ABC12345");

        new OrderShippedListener(emailService).handleOrderShipped(event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(emailService).sendHtmlEmail(
                eq("cliente@smartlogix.cl"),
                eq("Tu pedido está en camino — SmartLogix"),
                eq("order-shipped"),
                contextCaptor.capture()
        );
        assertThat(contextCaptor.getValue().getVariable("trackingNumber")).isEqualTo("SL-ABC12345");
    }

    @Test
    void orderDelivered_sendsDeliveryEmailWithTrackingNumber() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "SL-ABC12345", "cliente@smartlogix.cl");

        new OrderDeliveredListener(emailService).handleOrderDelivered(event);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(emailService).sendHtmlEmail(
                eq("cliente@smartlogix.cl"),
                eq("Tu pedido fue entregado — SmartLogix"),
                eq("order-delivered"),
                contextCaptor.capture()
        );
        assertThat(contextCaptor.getValue().getVariable("orderId")).isEqualTo("order-1");
        assertThat(contextCaptor.getValue().getVariable("trackingNumber")).isEqualTo("SL-ABC12345");
    }

    @Test
    void orderDelivered_ignoresEventWithoutCustomerEmail() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "SL-ABC12345", null);

        new OrderDeliveredListener(emailService).handleOrderDelivered(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void orderRejected_createsInAppNotificationAndSendsEmail() {
        ReservationFailedEvent event = new ReservationFailedEvent(
                "order-1",
                "product-1",
                "Stock insuficiente",
                "cliente@smartlogix.cl",
                "Cliente Demo"
        );

        new OrderRejectedListener(notificationService, emailService).handleOrderRejected(event);

        verify(notificationService).createInAppNotification(
                eq("order-1"),
                eq("cliente@smartlogix.cl"),
                eq("Pedido rechazado por falta de stock"),
                eq("El pedido order-1 fue rechazado. Motivo: Stock insuficiente")
        );
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(emailService).sendHtmlEmail(
                eq("cliente@smartlogix.cl"),
                eq("Tu pedido no pudo ser procesado — SmartLogix"),
                eq("order-rejected"),
                contextCaptor.capture()
        );
        assertThat(contextCaptor.getValue().getVariable("reason")).isEqualTo("Stock insuficiente");
    }

    @Test
    void orderRejected_withoutCustomerEmailCreatesSystemInAppOnly() {
        ReservationFailedEvent event = new ReservationFailedEvent(
                "order-1",
                "product-1",
                "Stock insuficiente",
                null,
                "Cliente Demo"
        );

        new OrderRejectedListener(notificationService, emailService).handleOrderRejected(event);

        verify(notificationService).createInAppNotification(
                eq("order-1"),
                eq("SYSTEM"),
                eq("Pedido rechazado por falta de stock"),
                eq("El pedido order-1 fue rechazado. Motivo: Stock insuficiente")
        );
        verifyNoInteractions(emailService);
    }

    @Test
    void orderRejected_wrapsFailuresAsAmqpReject() {
        ReservationFailedEvent event = new ReservationFailedEvent(
                "order-1",
                "product-1",
                "Stock insuficiente",
                "cliente@smartlogix.cl",
                "Cliente Demo"
        );
        doThrow(new RuntimeException("db down")).when(notificationService)
                .createInAppNotification(eq("order-1"), eq("cliente@smartlogix.cl"), any(), any());

        assertThatThrownBy(() -> new OrderRejectedListener(notificationService, emailService).handleOrderRejected(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("order-1");
    }
}
