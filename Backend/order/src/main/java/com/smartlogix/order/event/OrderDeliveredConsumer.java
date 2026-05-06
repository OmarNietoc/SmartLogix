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
public class OrderDeliveredConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @RabbitListener(queues = "order.delivered.queue")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Entrega confirmada: orderId={}, tracking={}", event.orderId(), event.trackingNumber());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus().canTransitionTo(OrderStatus.DELIVERED)) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Order {} → DELIVERED", event.orderId());
            }
        });
    }
}
