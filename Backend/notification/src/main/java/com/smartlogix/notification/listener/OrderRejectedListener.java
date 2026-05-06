package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.event.ReservationFailedEvent;
import com.smartlogix.notification.service.EmailService;
import com.smartlogix.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRejectedListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_REJECTED)
    public void handleOrderRejected(ReservationFailedEvent event) {
        log.info("Procesando rechazo de orden orderId={}", event.orderId());

        // In-app notification (campanita) — siempre, sin importar si hay email
        notificationService.createInAppNotification(
                event.orderId(),
                event.customerEmail() != null ? event.customerEmail() : "SYSTEM",
                "Pedido rechazado por falta de stock",
                "El pedido " + event.orderId() + " fue rechazado. Motivo: " + event.reason()
        );

        // Email al cliente si tiene dirección
        if (event.customerEmail() == null) return;
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("customerName", event.customerName());
        ctx.setVariable("reason", event.reason());
        emailService.sendHtmlEmail(
                event.customerEmail(),
                "Tu pedido no pudo ser procesado — SmartLogix",
                "order-rejected",
                ctx
        );
    }
}
