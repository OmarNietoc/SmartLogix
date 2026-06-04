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
class ReservationConfirmedConsumerTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private ReservationConfirmedConsumer consumer;

    @Test
    @DisplayName("handleReservationConfirmed transitions PENDING order to CONFIRMED")
    void handleReservationConfirmed_pendingOrder_transitionsToConfirmed() {
        Order order = buildOrder("order-1", OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        consumer.handleReservationConfirmed(new ReservationConfirmedEvent("order-1", "a@b.com", "Ana", "Calle 1"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("handleReservationConfirmed skips order not found")
    void handleReservationConfirmed_orderNotFound_doesNothing() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        consumer.handleReservationConfirmed(new ReservationConfirmedEvent("bad", "a@b.com", "Ana", "Calle 1"));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleReservationConfirmed skips DELIVERED order (invalid transition)")
    void handleReservationConfirmed_deliveredOrder_skipsTransition() {
        Order order = buildOrder("order-1", OrderStatus.DELIVERED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        consumer.handleReservationConfirmed(new ReservationConfirmedEvent("order-1", "a@b.com", "Ana", "Calle 1"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleReservationConfirmed wraps repository exception as AMQP reject")
    void handleReservationConfirmed_repositoryThrows_wrapsAsAmqpException() {
        when(orderRepository.findById("order-1")).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() ->
                consumer.handleReservationConfirmed(new ReservationConfirmedEvent("order-1", "a@b.com", "Ana", "Calle 1")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    private Order buildOrder(String id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }
}
