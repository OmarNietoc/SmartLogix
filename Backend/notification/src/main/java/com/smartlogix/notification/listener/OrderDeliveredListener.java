package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.event.OrderDeliveredEvent;
import com.smartlogix.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeliveredListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_DELIVERED)
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        if (event.customerEmail() == null) return;
        try {
            log.info("Enviando email de entrega para orderId={}", event.orderId());
            Context ctx = new Context();
            ctx.setVariable("orderId", event.orderId());
            ctx.setVariable("trackingNumber", event.trackingNumber());
            emailService.sendHtmlEmail(
                    event.customerEmail(),
                    "Tu pedido fue entregado — SmartLogix",
                    "order-delivered",
                    ctx
            );
        } catch (Exception e) {
            log.error("Error notificando delivered orderId={}: {}", event.orderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Error en OrderDeliveredListener orderId=" + event.orderId(), e);
        }
    }
}
