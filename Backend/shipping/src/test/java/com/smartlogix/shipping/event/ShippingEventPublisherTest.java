package com.smartlogix.shipping.event;

import com.smartlogix.shipping.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShippingEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @Test
    void publishOrderShipped_sendsEventWithConfiguredRoutingKey() {
        OrderShippedEvent event = new OrderShippedEvent("order-1", "cliente@smartlogix.cl", "SL-ABC12345");

        new ShippingEventPublisher(rabbitTemplate).publishOrderShipped(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ORDER_SHIPPED,
                event
        );
    }

    @Test
    void publishOrderDelivered_sendsEventWithConfiguredRoutingKey() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "SL-ABC12345", "cliente@smartlogix.cl");

        new ShippingEventPublisher(rabbitTemplate).publishOrderDelivered(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ORDER_DELIVERED,
                event
        );
    }
}
