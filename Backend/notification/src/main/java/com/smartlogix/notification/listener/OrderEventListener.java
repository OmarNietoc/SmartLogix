package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.event.OrderEvent;
import com.smartlogix.notification.service.EmailService;
import com.smartlogix.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CREATED)
    public void handleOrderCreated(OrderEvent event) {
        try {
            log.info("Recibido OrderEvent para orden id={} estado={}", event.getOrderId(), event.getStatus());

            CreateNotificationRequest request = new CreateNotificationRequest(
                    event.getOrderId(),
                    event.getCustomerEmail(),
                    event.getSubject(),
                    event.getMessage()
            );
            notificationService.createNotification(request);

            Context ctx = new Context();
            ctx.setVariable("orderId", event.getOrderId());
            ctx.setVariable("customerName", event.getCustomerName());
            ctx.setVariable("message", event.getMessage());
            emailService.sendHtmlEmail(
                    event.getCustomerEmail(),
                    event.getSubject() != null ? event.getSubject() : "Confirmación de pedido — SmartLogix",
                    "order-created",
                    ctx
            );
        } catch (Exception e) {
            log.error("Error procesando OrderEvent orderId={}: {}", event.getOrderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Error en OrderEventListener orderId=" + event.getOrderId(), e);
        }
    }
}
