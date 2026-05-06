package com.smartlogix.shipping.event;

import com.smartlogix.shipping.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderShipped(OrderShippedEvent event) {
        log.info("Publicando order.shipped para orderId={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ORDER_SHIPPED, event);
    }

    public void publishOrderDelivered(OrderDeliveredEvent event) {
        log.info("Publicando order.delivered para orderId={}", event.orderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ORDER_DELIVERED, event);
    }
}
