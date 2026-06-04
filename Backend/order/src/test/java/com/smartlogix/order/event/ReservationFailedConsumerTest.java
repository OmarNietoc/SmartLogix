package com.smartlogix.order.event;

import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationFailedConsumerTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private ReservationFailedConsumer consumer;

    @Test
    @DisplayName("handleReservationFailed transitions order to REJECTED")
    void handleReservationFailed_existingOrder_transitionsToRejected() {
        Order order = buildOrder("order-1", OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        consumer.handleReservationFailed(
                new ReservationFailedEvent("order-1", "prod-1", "stock insuficiente", "a@b.com", "Ana"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("handleReservationFailed skips when order not found")
    void handleReservationFailed_orderNotFound_doesNothing() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        consumer.handleReservationFailed(
                new ReservationFailedEvent("bad", "prod-1", "reason", "a@b.com", "Ana"));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleReservationFailed wraps repository exception as AMQP reject")
    void handleReservationFailed_repositoryThrows_wrapsAsAmqpException() {
        when(orderRepository.findById("order-1")).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() ->
                consumer.handleReservationFailed(
                        new ReservationFailedEvent("order-1", "prod-1", "reason", "a@b.com", "Ana")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    private Order buildOrder(String id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }
}
