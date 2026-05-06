package com.smartlogix.order.event;

import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderShippedConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @RabbitListener(queues = "order.shipped.queue")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("Envío despachado: orderId={}, tracking={}", event.orderId(), event.trackingNumber());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus().canTransitionTo(OrderStatus.SHIPPED)) {
                order.setStatus(OrderStatus.SHIPPED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Order {} → SHIPPED (tracking={})", event.orderId(), event.trackingNumber());
            }
        });
    }
}
