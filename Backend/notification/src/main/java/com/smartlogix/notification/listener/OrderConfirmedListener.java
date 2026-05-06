package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.event.ReservationConfirmedEvent;
import com.smartlogix.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CONFIRMED)
    public void handleOrderConfirmed(ReservationConfirmedEvent event) {
        if (event.customerEmail() == null) return;
        log.info("Enviando email de confirmación para orderId={}", event.orderId());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("customerName", event.customerName());
        emailService.sendHtmlEmail(
                event.customerEmail(),
                "Tu pedido fue confirmado — SmartLogix",
                "order-confirmed",
                ctx
        );
    }
}
