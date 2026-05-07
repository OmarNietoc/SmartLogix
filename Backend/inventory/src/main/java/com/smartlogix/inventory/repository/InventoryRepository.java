package com.smartlogix.inventory.repository;
import com.smartlogix.inventory.model.Inventory; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface InventoryRepository extends JpaRepository<Inventory, String> { Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId); List<Inventory> findByProductId(String productId); List<Inventory> findByWarehouseId(String warehouseId); List<Inventory> findByProductCompanyId(String companyId); }
