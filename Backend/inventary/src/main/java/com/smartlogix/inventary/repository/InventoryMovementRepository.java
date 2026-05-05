package com.smartlogix.inventary.repository;
import com.smartlogix.inventary.model.InventoryMovement; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> { List<InventoryMovement> findByInventoryId(String inventoryId); }
