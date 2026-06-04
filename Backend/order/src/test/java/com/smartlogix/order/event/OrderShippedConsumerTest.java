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
class OrderShippedConsumerTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderShippedConsumer consumer;

    @Test
    @DisplayName("handleOrderShipped transitions CONFIRMED order to SHIPPED")
    void handleOrderShipped_confirmedOrder_transitionsToShipped() {
        Order order = buildOrder("order-1", OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        consumer.handleOrderShipped(new OrderShippedEvent("order-1", "a@b.com", "TRK-002"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("handleOrderShipped skips PENDING order (invalid transition)")
    void handleOrderShipped_pendingOrder_skipsTransition() {
        Order order = buildOrder("order-1", OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        consumer.handleOrderShipped(new OrderShippedEvent("order-1", "a@b.com", "TRK-002"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleOrderShipped skips when order not found")
    void handleOrderShipped_orderNotFound_doesNothing() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        consumer.handleOrderShipped(new OrderShippedEvent("bad", "a@b.com", "TRK-002"));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleOrderShipped wraps exception as AMQP reject")
    void handleOrderShipped_repositoryThrows_wrapsAsAmqpException() {
        when(orderRepository.findById("order-1")).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() ->
                consumer.handleOrderShipped(new OrderShippedEvent("order-1", "a@b.com", "TRK-002")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    private Order buildOrder(String id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }
}
