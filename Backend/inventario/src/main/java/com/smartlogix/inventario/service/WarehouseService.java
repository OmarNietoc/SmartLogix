package com.smartlogix.inventario.service;

import com.smartlogix.inventario.enums.WarehouseType;
import com.smartlogix.inventario.exception.WarehouseNotFoundException;
import com.smartlogix.inventario.model.Warehouse;
import com.smartlogix.inventario.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    public List<Warehouse> getAllWarehouses(String companyId, WarehouseType type) {
        if (companyId != null && type != null) return warehouseRepository.findByCompanyIdAndType(companyId, type);
        if (companyId != null) return warehouseRepository.findByCompanyId(companyId);
        if (type != null) return warehouseRepository.findByType(type);
        return warehouseRepository.findAll();
    }
    public Warehouse getWarehouseById(String id) { return warehouseRepository.findById(id).orElseThrow(() -> new WarehouseNotFoundException("La bodega con ID " + id + " no fue encontrada.")); }
    @Transactional public Warehouse createWarehouse(Warehouse warehouse) { validate(warehouse); log.info("Creando bodega {}", warehouse.getName()); return warehouseRepository.save(warehouse); }
    @Transactional public Warehouse updateWarehouse(String id, Warehouse warehouse) { Warehouse e=getWarehouseById(id); validate(warehouse); e.setCompanyId(warehouse.getCompanyId()); e.setName(warehouse.getName()); e.setLocationAddress(warehouse.getLocationAddress()); e.setType(warehouse.getType()); return warehouseRepository.save(e); }
    @Transactional public void deleteWarehouse(String id) { warehouseRepository.delete(getWarehouseById(id)); }
    private void validate(Warehouse warehouse) { if (warehouse.getCompanyId()==null||warehouse.getCompanyId().isBlank()) throw new IllegalArgumentException("companyId es obligatorio"); if (warehouse.getName()==null||warehouse.getName().isBlank()) throw new IllegalArgumentException("name es obligatorio"); if (warehouse.getLocationAddress()==null||warehouse.getLocationAddress().isBlank()) throw new IllegalArgumentException("locationAddress es obligatorio"); if (warehouse.getType()==null) throw new IllegalArgumentException("type es obligatorio"); }
}
