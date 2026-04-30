package com.smartlogix.inventario.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Catálogo de productos de una PYME. companyId es referencia lógica a ms-users. */
@Entity
@Table(name = "products")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "company_id", nullable = false)
    private String companyId;
    @Column(nullable = false, unique = true, length = 50)
    private String sku;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
