package com.smartlogix.inventory.service;

import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.exception.WarehouseNotFoundException;
import com.smartlogix.inventory.model.Warehouse;
import com.smartlogix.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> getAllWarehouses(String companyId, WarehouseType type) {
        requireCompanyId(companyId);
        if (type != null) {
            return warehouseRepository.findByCompanyIdAndType(companyId, type);
        }
        return warehouseRepository.findByCompanyId(companyId);
    }

    public Warehouse getWarehouseById(String id, String companyId) {
        requireCompanyId(companyId);
        return warehouseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new WarehouseNotFoundException("La bodega con ID " + id + " no fue encontrada."));
    }

    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse, String companyId) {
        requireCompanyId(companyId);
        warehouse.setCompanyId(companyId);
        validate(warehouse);
        log.info("Creando bodega {} para compania {}", warehouse.getName(), companyId);
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(String id, Warehouse warehouse, String companyId) {
        Warehouse existing = getWarehouseById(id, companyId);
        warehouse.setCompanyId(companyId);
        validate(warehouse);
        existing.setName(warehouse.getName());
        existing.setLocationAddress(warehouse.getLocationAddress());
        existing.setType(warehouse.getType());
        return warehouseRepository.save(existing);
    }

    @Transactional
    public void deleteWarehouse(String id, String companyId) {
        Warehouse warehouse = getWarehouseById(id, companyId);
        warehouse.setStatus("INACTIVE");
        warehouseRepository.save(warehouse);
    }

    private void requireCompanyId(String companyId) {
        if (companyId == null || companyId.isBlank()) {
            throw new IllegalArgumentException("X-Company-Id es obligatorio");
        }
    }

    private void validate(Warehouse warehouse) {
        if (warehouse.getCompanyId() == null || warehouse.getCompanyId().isBlank()) {
            throw new IllegalArgumentException("companyId es obligatorio");
        }
        if (warehouse.getName() == null || warehouse.getName().isBlank()) {
            throw new IllegalArgumentException("name es obligatorio");
        }
        if (warehouse.getLocationAddress() == null || warehouse.getLocationAddress().isBlank()) {
            throw new IllegalArgumentException("locationAddress es obligatorio");
        }
        if (warehouse.getType() == null) {
            throw new IllegalArgumentException("type es obligatorio");
        }
    }
}
