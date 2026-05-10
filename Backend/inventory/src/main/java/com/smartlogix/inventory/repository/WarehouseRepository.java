package com.smartlogix.inventory.repository;

import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    List<Warehouse> findByCompanyId(String companyId);

    List<Warehouse> findByCompanyIdAndType(String companyId, WarehouseType type);

    Optional<Warehouse> findByIdAndCompanyId(String id, String companyId);
}
