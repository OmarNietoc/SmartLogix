package com.smartlogix.inventary.model;

import com.smartlogix.inventary.enums.WarehouseType;
import jakarta.persistence.*;
import lombok.*;

/** Bodega o tienda física donde se administra stock. */
@Entity
@Table(name = "warehouses")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "company_id", nullable = false)
    private String companyId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "location_address", nullable = false, columnDefinition = "TEXT")
    private String locationAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WarehouseType type;
}
