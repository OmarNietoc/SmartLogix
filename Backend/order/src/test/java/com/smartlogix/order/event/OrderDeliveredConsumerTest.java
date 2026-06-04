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
class OrderDeliveredConsumerTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderDeliveredConsumer consumer;

    @Test
    @DisplayName("handleOrderDelivered transitions SHIPPED order to DELIVERED")
    void handleOrderDelivered_shippedOrder_transitionsToDelivered() {
        Order order = buildOrder("order-1", OrderStatus.SHIPPED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        consumer.handleOrderDelivered(new OrderDeliveredEvent("order-1", "TRK-001", "a@b.com"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("handleOrderDelivered skips PENDING order (invalid transition)")
    void handleOrderDelivered_pendingOrder_skipsTransition() {
        Order order = buildOrder("order-1", OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        consumer.handleOrderDelivered(new OrderDeliveredEvent("order-1", "TRK-001", "a@b.com"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleOrderDelivered skips when order not found")
    void handleOrderDelivered_orderNotFound_doesNothing() {
        when(orderRepository.findById("bad")).thenReturn(Optional.empty());

        consumer.handleOrderDelivered(new OrderDeliveredEvent("bad", "TRK-001", "a@b.com"));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleOrderDelivered wraps exception as AMQP reject")
    void handleOrderDelivered_repositoryThrows_wrapsAsAmqpException() {
        when(orderRepository.findById("order-1")).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() ->
                consumer.handleOrderDelivered(new OrderDeliveredEvent("order-1", "TRK-001", "a@b.com")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    private Order buildOrder(String id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }
}
