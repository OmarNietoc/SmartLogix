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
public class ReservationFailedConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @RabbitListener(queues = "order.failed.queue")
    public void handleReservationFailed(ReservationFailedEvent event) {
        log.warn("Saga compensación recibida: orderId={}, productId={}, reason={}",
                event.orderId(), event.productId(), event.reason());
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.RECHAZADO);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order {} marcado RECHAZADO por fallo en reserva de stock", event.orderId());
        });
    }
}
