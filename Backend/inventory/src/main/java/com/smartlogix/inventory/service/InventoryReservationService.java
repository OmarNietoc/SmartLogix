package com.smartlogix.inventory.service;

import com.smartlogix.inventory.dto.StockReservationRequestDTO;
import com.smartlogix.inventory.enums.MovementType;
import com.smartlogix.inventory.enums.ReservationStatus;
import com.smartlogix.inventory.exception.InsufficientStockException;
import com.smartlogix.inventory.exception.InventoryReservationNotFoundException;
import com.smartlogix.inventory.model.Inventory;
import com.smartlogix.inventory.model.InventoryReservation;
import com.smartlogix.inventory.repository.InventoryRepository;
import com.smartlogix.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationService {
    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    public List<InventoryReservation> getAllReservations(String orderId, ReservationStatus status) {
        if (orderId != null) return reservationRepository.findByOrderId(orderId);
        if (status != null) return reservationRepository.findByStatus(status);
        return reservationRepository.findAll();
    }

    public InventoryReservation getReservationById(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new InventoryReservationNotFoundException("La reserva con ID " + id + " no fue encontrada."));
    }

    @Transactional
    public InventoryReservation reserveStock(StockReservationRequestDTO req) {
        validate(req);
        Inventory inventory = inventoryService.getInventoryByProductAndWarehouse(req.getProductId(), req.getWarehouseId());

        var existing = reservationRepository.findByOrderIdAndProductIdAndWarehouseIdAndStatus(
                req.getOrderId(), req.getProductId(), req.getWarehouseId(), ReservationStatus.RESERVED);
        if (existing.isPresent()) return existing.get();

        if (inventory.getStockAvailable() < req.getQuantity()) {
            throw new InsufficientStockException("Stock insuficiente para reservar el pedido " + req.getOrderId());
        }

        inventory.setStockAvailable(inventory.getStockAvailable() - req.getQuantity());
        inventory.setStockReserved(inventory.getStockReserved() + req.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        inventoryService.registerMovement(savedInventory, MovementType.RESERVED, req.getQuantity(), "Reserva Saga pedido " + req.getOrderId());

        InventoryReservation reservation = InventoryReservation.builder()
                .orderId(req.getOrderId())
                .product(savedInventory.getProduct())
                .warehouse(savedInventory.getWarehouse())
                .quantity(req.getQuantity())
                .status(ReservationStatus.RESERVED)
                .build();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public InventoryReservation compensateReservation(String reservationId) {
        InventoryReservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("La reserva no está en estado RESERVED");
        }

        Inventory inventory = inventoryService.getInventoryByProductAndWarehouse(
                reservation.getProduct().getId(), reservation.getWarehouse().getId());
        inventory.setStockAvailable(inventory.getStockAvailable() + reservation.getQuantity());
        inventory.setStockReserved(inventory.getStockReserved() - reservation.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        inventoryService.registerMovement(savedInventory, MovementType.COMPENSATED, reservation.getQuantity(), "Compensación reserva " + reservation.getId());

        reservation.setStatus(ReservationStatus.COMPENSATED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public InventoryReservation confirmReservationAsOutput(String reservationId) {
        InventoryReservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("La reserva no está en estado RESERVED");
        }

        Inventory inventory = inventoryService.getInventoryByProductAndWarehouse(
                reservation.getProduct().getId(), reservation.getWarehouse().getId());
        inventory.setStockReserved(inventory.getStockReserved() - reservation.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        inventoryService.registerMovement(savedInventory, MovementType.OUT, -reservation.getQuantity(), "Salida definitiva reserva " + reservation.getId());

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void compensateAllForOrder(String orderId) {
        List<InventoryReservation> toCompensate = reservationRepository.findByOrderId(orderId)
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED)
                .toList();
        for (InventoryReservation r : toCompensate) {
            try {
                compensateReservation(r.getId());
            } catch (Exception e) {
                log.error("Error compensando reserva id={} orderId={}: {}", r.getId(), orderId, e.getMessage());
            }
        }
        log.info("Compensadas {}/{} reservas para orderId={}", toCompensate.size(), toCompensate.size(), orderId);
    }

    private void validate(StockReservationRequestDTO req) {
        if (req.getOrderId() == null || req.getOrderId().isBlank()) throw new IllegalArgumentException("orderId es obligatorio");
        if (req.getProductId() == null || req.getProductId().isBlank()) throw new IllegalArgumentException("productId es obligatorio");
        if (req.getWarehouseId() == null || req.getWarehouseId().isBlank()) throw new IllegalArgumentException("warehouseId es obligatorio");
        if (req.getQuantity() == null || req.getQuantity() <= 0) throw new IllegalArgumentException("quantity debe ser mayor a cero");
    }
}
