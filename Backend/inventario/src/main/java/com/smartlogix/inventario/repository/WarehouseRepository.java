package com.smartlogix.inventario.repository;
import com.smartlogix.inventario.enums.WarehouseType; import com.smartlogix.inventario.model.Warehouse; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface WarehouseRepository extends JpaRepository<Warehouse, String> { List<Warehouse> findByCompanyId(String companyId); List<Warehouse> findByType(WarehouseType type); List<Warehouse> findByCompanyIdAndType(String companyId, WarehouseType type); }
