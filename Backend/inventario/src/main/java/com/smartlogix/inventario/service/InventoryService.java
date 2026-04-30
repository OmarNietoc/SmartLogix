package com.smartlogix.inventario.service;

import com.smartlogix.inventario.dto.InventoryCreationRequestDTO;
import com.smartlogix.inventario.dto.StockAdjustmentRequestDTO;
import com.smartlogix.inventario.enums.MovementType;
import com.smartlogix.inventario.exception.*;
import com.smartlogix.inventario.model.*;
import com.smartlogix.inventario.repository.*;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository; private final ProductService productService; private final WarehouseService warehouseService; private final InventoryMovementRepository movementRepository;
    public List<Inventory> getAllInventory(String companyId, String productId, String warehouseId) { if (productId!=null) return inventoryRepository.findByProductId(productId); if (warehouseId!=null) return inventoryRepository.findByWarehouseId(warehouseId); if (companyId!=null) return inventoryRepository.findByProductCompanyId(companyId); return inventoryRepository.findAll(); }
    public Inventory getInventoryById(String id) { return inventoryRepository.findById(id).orElseThrow(() -> new InventoryNotFoundException("El inventario con ID " + id + " no fue encontrado.")); }
    public Inventory getInventoryByProductAndWarehouse(String productId, String warehouseId) { return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId).orElseThrow(() -> new InventoryNotFoundException("No existe inventario para el producto " + productId + " en la bodega " + warehouseId)); }
    @Transactional public Inventory createInventory(InventoryCreationRequestDTO req) { if (req.getStockAvailable()==null||req.getStockAvailable()<0) throw new IllegalArgumentException("stockAvailable debe ser mayor o igual a cero"); Product product=productService.getProductById(req.getProductId()); Warehouse warehouse=warehouseService.getWarehouseById(req.getWarehouseId()); inventoryRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).ifPresent(i -> { throw new IllegalStateException("Ya existe inventario para ese producto y bodega"); }); Inventory inv=Inventory.builder().product(product).warehouse(warehouse).stockAvailable(req.getStockAvailable()).stockReserved(0).build(); Inventory saved=inventoryRepository.save(inv); registerMovement(saved, MovementType.IN, req.getStockAvailable(), "Stock inicial"); return saved; }
    @Transactional public Inventory increaseStock(String id, StockAdjustmentRequestDTO req) { Inventory inv=getInventoryById(id); int q=validQuantity(req.getQuantity()); inv.setStockAvailable(inv.getStockAvailable()+q); Inventory saved=inventoryRepository.save(inv); registerMovement(saved, MovementType.IN, q, req.getReason()); return saved; }
    @Transactional public Inventory decreaseStock(String id, StockAdjustmentRequestDTO req) { Inventory inv=getInventoryById(id); int q=validQuantity(req.getQuantity()); if (inv.getStockAvailable()<q) throw new InsufficientStockException("Stock disponible insuficiente para descontar " + q + " unidades."); inv.setStockAvailable(inv.getStockAvailable()-q); Inventory saved=inventoryRepository.save(inv); registerMovement(saved, MovementType.OUT, -q, req.getReason()); return saved; }
    public List<InventoryMovement> getMovements(String inventoryId) { getInventoryById(inventoryId); return movementRepository.findByInventoryId(inventoryId); }
    public void registerMovement(Inventory inventory, MovementType type, Integer quantity, String reason) { movementRepository.save(InventoryMovement.builder().inventory(inventory).movementType(type).quantity(quantity).reason(reason).build()); }
    private int validQuantity(Integer quantity) { if (quantity==null||quantity<=0) throw new IllegalArgumentException("quantity debe ser mayor a cero"); return quantity; }
}
