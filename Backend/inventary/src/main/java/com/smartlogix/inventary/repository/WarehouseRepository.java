package com.smartlogix.inventary.repository;
import com.smartlogix.inventary.enums.WarehouseType; import com.smartlogix.inventary.model.Warehouse; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface WarehouseRepository extends JpaRepository<Warehouse, String> { List<Warehouse> findByCompanyId(String companyId); List<Warehouse> findByType(WarehouseType type); List<Warehouse> findByCompanyIdAndType(String companyId, WarehouseType type); }
