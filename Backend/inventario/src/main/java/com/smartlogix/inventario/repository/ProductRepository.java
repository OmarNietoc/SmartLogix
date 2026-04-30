package com.smartlogix.inventario.repository;
import com.smartlogix.inventario.model.Product; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface ProductRepository extends JpaRepository<Product, String> { Optional<Product> findBySku(String sku); List<Product> findByCompanyId(String companyId); boolean existsBySku(String sku); }
