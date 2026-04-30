package com.smartlogix.inventario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartlogix.inventario.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Reserva idempotente de stock asociada a un pedido. */
@Entity
@Table(name = "inventory_reservations", indexes = @Index(name = "idx_inventory_reservation_order", columnList = "order_id"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryReservation {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "order_id", nullable = false)
    private String orderId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;
    @Column(nullable = false)
    private Integer quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ReservationStatus status;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
