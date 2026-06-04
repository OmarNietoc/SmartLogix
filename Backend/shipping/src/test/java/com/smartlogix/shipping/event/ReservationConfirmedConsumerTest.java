package com.smartlogix.shipping.event;

import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmedConsumerTest {

    @Mock private ShipmentRepository shipmentRepository;
    @InjectMocks private ReservationConfirmedConsumer consumer;

    @Test
    @DisplayName("handleReservationConfirmed creates shipment with PENDING status")
    void handleReservationConfirmed_validEvent_createsShipment() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1", "ana@test.com", "Ana", "Calle 1", "company-1");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.handleReservationConfirmed(event);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        Shipment saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo("order-1");
        assertThat(saved.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(saved.getCompanyId()).isEqualTo("company-1");
        assertThat(saved.getTrackingNumber()).startsWith("SL-");
    }

    @Test
    @DisplayName("handleReservationConfirmed uses default address when shippingAddress is null")
    void handleReservationConfirmed_nullAddress_usesDefaultAddress() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1", "ana@test.com", "Ana", null, "company-1");
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.handleReservationConfirmed(event);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getShippingAddress()).isEqualTo("Pendiente de asignación");
    }

    @Test
    @DisplayName("handleReservationConfirmed wraps repository exception as AMQP reject")
    void handleReservationConfirmed_repositoryThrows_wrapsAsAmqpException() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1", "ana@test.com", "Ana", "Calle 1", "company-1");
        when(shipmentRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> consumer.handleReservationConfirmed(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
