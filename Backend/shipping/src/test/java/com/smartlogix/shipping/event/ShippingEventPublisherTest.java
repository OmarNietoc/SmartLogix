package com.smartlogix.shipping.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private ShippingEventPublisher publisher;

    @Test
    @DisplayName("publishOrderShipped sends order.shipped event via RabbitMQ")
    void publishOrderShipped_callsRabbitTemplate() {
        OrderShippedEvent event = new OrderShippedEvent("order-1", "ana@test.com", "TRK-001");

        publisher.publishOrderShipped(event);

        verify(rabbitTemplate).convertAndSend(
                anyString(), anyString(), eq(event));
    }

    @Test
    @DisplayName("publishOrderDelivered sends order.delivered event via RabbitMQ")
    void publishOrderDelivered_callsRabbitTemplate() {
        OrderDeliveredEvent event = new OrderDeliveredEvent("order-1", "TRK-001", "ana@test.com");

        publisher.publishOrderDelivered(event);

        verify(rabbitTemplate).convertAndSend(
                anyString(), anyString(), eq(event));
    }
}
