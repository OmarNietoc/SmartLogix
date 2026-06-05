package com.smartlogix.shipping.event;

import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmedConsumerTest {

    @Mock private ShipmentRepository shipmentRepository;

    @Test
    void handleReservationConfirmed_createsPendingShipmentFromEvent() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1",
                "cliente@smartlogix.cl",
                "Cliente Demo",
                "Av. Demo 123",
                "company-1"
        );

        new ReservationConfirmedConsumer(shipmentRepository).handleReservationConfirmed(event);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        Shipment saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo("order-1");
        assertThat(saved.getCustomerEmail()).isEqualTo("cliente@smartlogix.cl");
        assertThat(saved.getCustomerName()).isEqualTo("Cliente Demo");
        assertThat(saved.getShippingAddress()).isEqualTo("Av. Demo 123");
        assertThat(saved.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(saved.getCompanyId()).isEqualTo("company-1");
        assertThat(saved.getTrackingNumber()).startsWith("SL-").hasSize(11);
    }

    @Test
    void handleReservationConfirmed_usesDefaultAddressWhenEventAddressIsNull() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1",
                "cliente@smartlogix.cl",
                "Cliente Demo",
                null,
                "company-1"
        );

        new ReservationConfirmedConsumer(shipmentRepository).handleReservationConfirmed(event);

        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getShippingAddress()).isEqualTo("Pendiente de asignación");
    }

    @Test
    void handleReservationConfirmed_wrapsRepositoryFailuresAsAmqpReject() {
        ReservationConfirmedEvent event = new ReservationConfirmedEvent(
                "order-1",
                "cliente@smartlogix.cl",
                "Cliente Demo",
                "Av. Demo 123",
                "company-1"
        );
        when(shipmentRepository.save(org.mockito.ArgumentMatchers.any(Shipment.class)))
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> new ReservationConfirmedConsumer(shipmentRepository).handleReservationConfirmed(event))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("order-1");
    }
}
