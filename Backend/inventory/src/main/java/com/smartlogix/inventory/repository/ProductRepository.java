package com.smartlogix.inventory.repository;

import com.smartlogix.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByIdAndCompanyId(String id, String companyId);
    Optional<Product> findBySkuAndCompanyId(String sku, String companyId);
    List<Product> findByCompanyId(String companyId);
    boolean existsBySku(String sku);
    boolean existsBySkuAndCompanyId(String sku, String companyId);
}
