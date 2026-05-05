package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.event.OrderShippedEvent;
import com.smartlogix.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderShippedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_SHIPPED)
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Notificando envío de orden id={} a {}", event.orderId(), event.customerEmail());
        Context ctx = new Context();
        ctx.setVariable("orderId", event.orderId());
        ctx.setVariable("trackingNumber", event.trackingNumber());
        emailService.sendHtmlEmail(
                event.customerEmail(),
                "Tu pedido está en camino — SmartLogix",
                "order-shipped",
                ctx
        );
    }
}
