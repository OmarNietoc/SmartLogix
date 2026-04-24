package com.smartlogix.inventario.repository;

import com.smartlogix.inventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring Data JPA crea la consulta automáticamente por el nombre del método
    Optional<Product> findBySku(String sku);
}