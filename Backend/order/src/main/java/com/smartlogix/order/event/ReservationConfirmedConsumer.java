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
public class ReservationConfirmedConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @RabbitListener(queues = "order.confirmed.queue")
    public void handleReservationConfirmed(ReservationConfirmedEvent event) {
        log.info("Todas las reservas confirmadas para orderId={}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus().canTransitionTo(OrderStatus.CONFIRMED)) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Order {} → CONFIRMED", event.orderId());
            }
        });
    }
}
