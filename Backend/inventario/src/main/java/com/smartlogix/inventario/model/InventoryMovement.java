package com.smartlogix.inventario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartlogix.inventario.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Bitácora de movimientos para auditoría y trazabilidad. */
@Entity
@Table(name = "inventory_movements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inventory_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Inventory inventory;
    @Enumerated(EnumType.STRING) @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;
    @Column(nullable = false)
    private Integer quantity;
    @Column(length = 200)
    private String reason;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
