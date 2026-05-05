package com.smartlogix.notification.listener;

import com.smartlogix.notification.config.RabbitMQConfig;
import com.smartlogix.notification.dto.CreateNotificationRequest;
import com.smartlogix.notification.event.OrderEvent;
import com.smartlogix.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received OrderEvent for Order ID: {} with Status: {}", event.getOrderId(), event.getStatus());

        CreateNotificationRequest request = new CreateNotificationRequest(
                event.getOrderId(),
                event.getCustomerEmail(),
                event.getSubject(),
                event.getMessage()
        );

        notificationService.createNotification(request);
    }
}
