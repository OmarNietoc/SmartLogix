package com.smartlogix.order.event;

import com.smartlogix.order.model.Order;
import com.smartlogix.order.model.OrderStatus;
import com.smartlogix.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumersTest {

    @Mock private OrderRepository orderRepository;

    @Test
    void reservationConfirmed_updatesPendingOrderToConfirmed() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        new ReservationConfirmedConsumer(orderRepository)
                .handleReservationConfirmed(new ReservationConfirmedEvent("order-1", "cliente@smartlogix.cl", "Cliente", "Av. Demo"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getUpdatedAt()).isNotNull();
        verify(orderRepository).save(order);
    }

    @Test
    void reservationConfirmed_doesNotSaveWhenTransitionIsInvalid() {
        Order order = order(OrderStatus.DELIVERED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        new ReservationConfirmedConsumer(orderRepository)
                .handleReservationConfirmed(new ReservationConfirmedEvent("order-1", "cliente@smartlogix.cl", "Cliente", "Av. Demo"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void reservationFailed_rejectsOrderAndSaves() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        new ReservationFailedConsumer(orderRepository)
                .handleReservationFailed(new ReservationFailedEvent("order-1", "product-1", "Stock insuficiente", "cliente@smartlogix.cl", "Cliente"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getUpdatedAt()).isNotNull();
        verify(orderRepository).save(order);
    }

    @Test
    void orderShipped_updatesConfirmedOrderToShipped() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        new OrderShippedConsumer(orderRepository)
                .handleOrderShipped(new OrderShippedEvent("order-1", "cliente@smartlogix.cl", "TRK-1"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    void orderDelivered_updatesShippedOrderToDelivered() {
        Order order = order(OrderStatus.SHIPPED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        new OrderDeliveredConsumer(orderRepository)
                .handleOrderDelivered(new OrderDeliveredEvent("order-1", "TRK-1", "cliente@smartlogix.cl"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void consumersWrapRepositoryFailuresAsAmqpReject() {
        when(orderRepository.findById("order-1")).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> new OrderShippedConsumer(orderRepository)
                .handleOrderShipped(new OrderShippedEvent("order-1", "cliente@smartlogix.cl", "TRK-1")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("order-1");
    }

    private Order order(OrderStatus status) {
        return Order.builder()
                .id("order-1")
                .status(status)
                .companyId("company-1")
                .build();
    }
}
