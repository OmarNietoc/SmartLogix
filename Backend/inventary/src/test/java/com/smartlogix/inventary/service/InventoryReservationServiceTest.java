package com.smartlogix.inventary.service;

import com.smartlogix.inventary.dto.StockReservationRequestDTO;
import com.smartlogix.inventary.enums.MovementType;
import com.smartlogix.inventary.enums.ReservationStatus;
<<<<<<< Updated upstream
=======
import com.smartlogix.inventary.enums.WarehouseType;
>>>>>>> Stashed changes
import com.smartlogix.inventary.exception.InsufficientStockException;
import com.smartlogix.inventary.exception.InventoryReservationNotFoundException;
import com.smartlogix.inventary.model.*;
import com.smartlogix.inventary.repository.InventoryRepository;
import com.smartlogix.inventary.repository.InventoryReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock private InventoryReservationRepository reservationRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private InventoryService inventoryService;

    @InjectMocks private InventoryReservationService reservationService;

    // ── reserveStock ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("reserveStock decreases available stock and increases reserved")
    void reserveStock_happyPath_updatesStockCounters() {
        StockReservationRequestDTO req = buildRequest("order-1", "prod-1", "ware-1", 5);
        Inventory inventory = buildInventory("inv-1", "prod-1", "ware-1", 10, 0);

        when(inventoryService.getInventoryByProductAndWarehouse("prod-1", "ware-1")).thenReturn(inventory);
        when(reservationRepository.findByOrderIdAndProductIdAndWarehouseIdAndStatus(
                "order-1", "prod-1", "ware-1", ReservationStatus.RESERVED)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryReservation result = reservationService.reserveStock(req);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(inventory.getStockAvailable()).isEqualTo(5);
        assertThat(inventory.getStockReserved()).isEqualTo(5);
        verify(inventoryService).registerMovement(any(), eq(MovementType.RESERVED), eq(5), contains("order-1"));
    }

    @Test
    @DisplayName("reserveStock throws InsufficientStockException when stock < quantity")
    void reserveStock_insufficientStock_throws() {
        StockReservationRequestDTO req = buildRequest("order-1", "prod-1", "ware-1", 15);
        Inventory inventory = buildInventory("inv-1", "prod-1", "ware-1", 10, 0);

        when(inventoryService.getInventoryByProductAndWarehouse("prod-1", "ware-1")).thenReturn(inventory);
        when(reservationRepository.findByOrderIdAndProductIdAndWarehouseIdAndStatus(
                any(), any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserveStock(req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("order-1");
    }

    @Test
    @DisplayName("reserveStock is idempotent: returns existing reservation without duplicate")
    void reserveStock_existingReservation_returnsExisting() {
        StockReservationRequestDTO req = buildRequest("order-1", "prod-1", "ware-1", 5);
        Inventory inventory = buildInventory("inv-1", "prod-1", "ware-1", 10, 5);
        InventoryReservation existing = InventoryReservation.builder()
                .id("res-1").orderId("order-1").quantity(5).status(ReservationStatus.RESERVED).build();

        when(inventoryService.getInventoryByProductAndWarehouse("prod-1", "ware-1")).thenReturn(inventory);
        when(reservationRepository.findByOrderIdAndProductIdAndWarehouseIdAndStatus(
                "order-1", "prod-1", "ware-1", ReservationStatus.RESERVED)).thenReturn(Optional.of(existing));

        InventoryReservation result = reservationService.reserveStock(req);

        assertThat(result.getId()).isEqualTo("res-1");
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("reserveStock validate: rejects null orderId")
    void reserveStock_nullOrderId_throwsIllegalArgument() {
        StockReservationRequestDTO req = buildRequest(null, "prod-1", "ware-1", 5);

        assertThatThrownBy(() -> reservationService.reserveStock(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    @DisplayName("reserveStock validate: rejects zero quantity")
    void reserveStock_zeroQuantity_throwsIllegalArgument() {
        StockReservationRequestDTO req = buildRequest("order-1", "prod-1", "ware-1", 0);

        assertThatThrownBy(() -> reservationService.reserveStock(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }

    // ── compensateReservation ─────────────────────────────────────────────────

    @Test
    @DisplayName("compensateReservation restores stock and sets COMPENSATED status")
    void compensateReservation_happyPath_restoresStock() {
        Product product = Product.builder().id("prod-1").sku("SKU1").name("Test").price(BigDecimal.TEN).build();
        Warehouse warehouse = buildWarehouse("ware-1");
        InventoryReservation reservation = InventoryReservation.builder()
                .id("res-1").orderId("order-1").product(product).warehouse(warehouse)
                .quantity(5).status(ReservationStatus.RESERVED).build();
        Inventory inventory = buildInventory("inv-1", "prod-1", "ware-1", 5, 5);

        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));
        when(inventoryService.getInventoryByProductAndWarehouse("prod-1", "ware-1")).thenReturn(inventory);
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryReservation result = reservationService.compensateReservation("res-1");

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.COMPENSATED);
        assertThat(inventory.getStockAvailable()).isEqualTo(10);
        assertThat(inventory.getStockReserved()).isEqualTo(0);
        verify(inventoryService).registerMovement(any(), eq(MovementType.COMPENSATED), eq(5), any());
    }

    @Test
    @DisplayName("compensateReservation throws when reservation is not RESERVED")
    void compensateReservation_alreadyCompensated_throwsIllegalState() {
        InventoryReservation reservation = InventoryReservation.builder()
                .id("res-1").status(ReservationStatus.COMPENSATED).build();

        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.compensateReservation("res-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    @DisplayName("compensateReservation throws when reservation not found")
    void compensateReservation_notFound_throws() {
        when(reservationRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.compensateReservation("bad"))
                .isInstanceOf(InventoryReservationNotFoundException.class);
    }

    // ── getAllReservations ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReservations filters by orderId when provided")
    void getAllReservations_withOrderId_callsFilteredQuery() {
        when(reservationRepository.findByOrderId("order-1")).thenReturn(List.of(
                InventoryReservation.builder().id("r1").orderId("order-1").build()
        ));

        List<InventoryReservation> result = reservationService.getAllReservations("order-1", null);

        assertThat(result).hasSize(1);
        verify(reservationRepository).findByOrderId("order-1");
    }

    @Test
    @DisplayName("getAllReservations with no filters returns all")
    void getAllReservations_noFilters_returnsAll() {
        when(reservationRepository.findAll()).thenReturn(List.of(
                InventoryReservation.builder().id("r1").build(),
                InventoryReservation.builder().id("r2").build()
        ));

        List<InventoryReservation> result = reservationService.getAllReservations(null, null);

        assertThat(result).hasSize(2);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private StockReservationRequestDTO buildRequest(String orderId, String productId, String warehouseId, Integer qty) {
        return StockReservationRequestDTO.builder()
                .orderId(orderId).productId(productId).warehouseId(warehouseId).quantity(qty).build();
    }

    private Inventory buildInventory(String id, String productId, String warehouseId, int available, int reserved) {
        Product product = Product.builder().id(productId).sku("SKU1").name("Test").price(BigDecimal.TEN).build();
        Warehouse warehouse = buildWarehouse(warehouseId);
        return Inventory.builder().id(id).product(product).warehouse(warehouse)
                .stockAvailable(available).stockReserved(reserved).build();
    }

    private Warehouse buildWarehouse(String id) {
        return Warehouse.builder().id(id).companyId("c1").name("Bodega Test")
<<<<<<< Updated upstream
                .locationAddress("Calle 1").type(com.smartlogix.inventary.enums.WarehouseType.WAREHOUSE).build();
=======
                .locationAddress("Calle 1").type(WarehouseType.WAREHOUSE).build();
>>>>>>> Stashed changes
    }
}
