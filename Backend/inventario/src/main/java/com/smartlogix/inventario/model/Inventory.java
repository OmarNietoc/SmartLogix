package com.smartlogix.inventario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Stock de un producto por bodega. Separa stock disponible y reservado para Saga. */
@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;
    @Column(name = "stock_available", nullable = false) @Builder.Default
    private Integer stockAvailable = 0;
    @Column(name = "stock_reserved", nullable = false) @Builder.Default
    private Integer stockReserved = 0;
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    @PrePersist @PreUpdate protected void touch() { lastUpdated = LocalDateTime.now(); }
}
