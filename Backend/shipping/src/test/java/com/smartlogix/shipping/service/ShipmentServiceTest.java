package com.smartlogix.shipping.service;

import com.smartlogix.shipping.enums.DeliveryStatus;
import com.smartlogix.shipping.event.ShippingEventPublisher;
import com.smartlogix.shipping.exception.ShipmentNotFoundException;
import com.smartlogix.shipping.model.Shipment;
import com.smartlogix.shipping.repository.RouteRepository;
import com.smartlogix.shipping.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private ShippingEventPublisher shippingEventPublisher;

    @InjectMocks private ShipmentService shipmentService;

    // ── getAllShipments ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllShipments without filter returns all")
    void getAllShipments_noFilter_returnsAll() {
        when(shipmentRepository.findAll()).thenReturn(List.of(buildShipment("s1"), buildShipment("s2")));

        List<Shipment> result = shipmentService.getAllShipments(null);

        assertThat(result).hasSize(2);
        verify(shipmentRepository).findAll();
        verify(shipmentRepository, never()).findByDeliveryStatus(any());
    }

    @Test
    @DisplayName("getAllShipments with filter delegates to filtered query")
    void getAllShipments_withFilter_callsFilteredQuery() {
        when(shipmentRepository.findByDeliveryStatus(DeliveryStatus.PENDING))
                .thenReturn(List.of(buildShipment("s1")));

        List<Shipment> result = shipmentService.getAllShipments(DeliveryStatus.PENDING);

        assertThat(result).hasSize(1);
        verify(shipmentRepository).findByDeliveryStatus(DeliveryStatus.PENDING);
        verify(shipmentRepository, never()).findAll();
    }

    // ── getShipmentById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getShipmentById found returns shipment")
    void getShipmentById_found_returnsShipment() {
        Shipment shipment = buildShipment("s1");
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));

        Shipment result = shipmentService.getShipmentById("s1");

        assertThat(result.getId()).isEqualTo("s1");
    }

    @Test
    @DisplayName("getShipmentById not found throws ShipmentNotFoundException")
    void getShipmentById_notFound_throws() {
        when(shipmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.getShipmentById("bad"))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining("bad");
    }

    // ── getShipmentByTrackingNumber ───────────────────────────────────────────

    @Test
    @DisplayName("getShipmentByTrackingNumber not found throws ShipmentNotFoundException")
    void getShipmentByTrackingNumber_notFound_throws() {
        when(shipmentRepository.findByTrackingNumber("TRK-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.getShipmentByTrackingNumber("TRK-999"))
                .isInstanceOf(ShipmentNotFoundException.class)
                .hasMessageContaining("TRK-999");
    }

    // ── createShipment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createShipment sets PENDING status when null and saves")
    void createShipment_nullStatus_setsPendingAndSaves() {
        Shipment input = buildShipment("s1");
        input.setDeliveryStatus(null);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(input);

        shipmentService.createShipment(input);

        assertThat(input.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
        verify(shipmentRepository).save(input);
    }

    @Test
    @DisplayName("createShipment preserves existing status")
    void createShipment_existingStatus_preserved() {
        Shipment input = buildShipment("s1");
        input.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(input);

        shipmentService.createShipment(input);

        assertThat(input.getDeliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
    }

    // ── updateShipmentStatus ──────────────────────────────────────────────────

    @Test
    @DisplayName("updateShipmentStatus PENDING->ASSIGNED valid transition saves")
    void updateShipmentStatus_validTransition_saves() {
        Shipment shipment = buildShipment("s1");
        shipment.setDeliveryStatus(DeliveryStatus.PENDING);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenReturn(shipment);

        Shipment result = shipmentService.updateShipmentStatus("s1", DeliveryStatus.ASSIGNED);

        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        verify(shipmentRepository).save(shipment);
    }

    @Test
    @DisplayName("updateShipmentStatus DISPATCHED publishes order.shipped event")
    void updateShipmentStatus_toDispatched_publishesShippedEvent() {
        Shipment shipment = buildShipment("s1");
        shipment.setDeliveryStatus(DeliveryStatus.ASSIGNED);
        shipment.setCustomerEmail("client@test.com");
        shipment.setOrderId("order-1");
        shipment.setTrackingNumber("TRK-001");
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenReturn(shipment);

        shipmentService.updateShipmentStatus("s1", DeliveryStatus.DISPATCHED);

        verify(shippingEventPublisher).publishOrderShipped(any());
        verify(shippingEventPublisher, never()).publishOrderDelivered(any());
    }

    @Test
    @DisplayName("updateShipmentStatus DELIVERED publishes order.delivered event and sets actualDelivery")
    void updateShipmentStatus_toDelivered_publishesDeliveredEvent() {
        Shipment shipment = buildShipment("s1");
        shipment.setDeliveryStatus(DeliveryStatus.DISPATCHED);
        shipment.setCustomerEmail("client@test.com");
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenReturn(shipment);

        shipmentService.updateShipmentStatus("s1", DeliveryStatus.DELIVERED);

        assertThat(shipment.getActualDelivery()).isNotNull();
        verify(shippingEventPublisher).publishOrderDelivered(any());
    }

    @Test
    @DisplayName("updateShipmentStatus invalid transition throws IllegalStateException")
    void updateShipmentStatus_invalidTransition_throws() {
        Shipment shipment = buildShipment("s1");
        shipment.setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus("s1", DeliveryStatus.PENDING))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── deleteShipment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteShipment sets status to CANCELLED (soft delete)")
    void deleteShipment_setsStatusCancelled() {
        Shipment shipment = buildShipment("s1");
        shipment.setDeliveryStatus(DeliveryStatus.PENDING);
        when(shipmentRepository.findById("s1")).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any())).thenReturn(shipment);

        shipmentService.deleteShipment("s1");

        assertThat(shipment.getDeliveryStatus()).isEqualTo(DeliveryStatus.CANCELLED);
        verify(shipmentRepository).save(shipment);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Shipment buildShipment(String id) {
        return Shipment.builder()
                .id(id)
                .orderId("order-" + id)
                .shippingAddress("Calle 1, Santiago, Chile")
                .deliveryStatus(DeliveryStatus.PENDING)
                .trackingNumber("TRK-" + id)
                .build();
    }
}
