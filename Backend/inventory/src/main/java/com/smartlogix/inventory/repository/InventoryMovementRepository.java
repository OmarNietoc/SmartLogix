package com.smartlogix.inventory.repository;
import com.smartlogix.inventory.model.InventoryMovement; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> { List<InventoryMovement> findByInventoryId(String inventoryId); }
