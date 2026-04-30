package com.smartlogix.inventario.repository;
import com.smartlogix.inventario.model.InventoryMovement; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> { List<InventoryMovement> findByInventoryId(String inventoryId); }
