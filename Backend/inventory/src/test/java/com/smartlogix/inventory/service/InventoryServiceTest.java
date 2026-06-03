package com.smartlogix.inventory.service;

import com.smartlogix.inventory.dto.InventoryCreationRequestDTO;
import com.smartlogix.inventory.dto.StockAdjustmentRequestDTO;
import com.smartlogix.inventory.exception.InventoryNotFoundException;
import com.smartlogix.inventory.exception.InsufficientStockException;
import com.smartlogix.inventory.model.*;
import com.smartlogix.inventory.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventoryRepository;
    @Mock ProductService productService;
    @Mock WarehouseService warehouseService;
    @Mock InventoryMovementRepository movementRepository;
    @InjectMocks InventoryService inventoryService;

    @Test
    void getInventoryById_found_returnsInventory() {
        // arrange
        Inventory inv = new Inventory();
        inv.setId("inv-1");
        when(inventoryRepository.findById("inv-1")).thenReturn(Optional.of(inv));

        // act
        Inventory result = inventoryService.getInventoryById("inv-1");

        // assert
        assertThat(result.getId()).isEqualTo("inv-1");
    }

    @Test
    void getInventoryById_notFound_throwsInventoryNotFoundException() {
        // arrange
        when(inventoryRepository.findById("missing")).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> inventoryService.getInventoryById("missing"))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getAllInventory_withCompanyId_filtersByCompany() {
        // arrange
        Inventory inv = new Inventory();
        when(inventoryRepository.findByProductCompanyId("company-1")).thenReturn(List.of(inv));

        // act
        List<Inventory> result = inventoryService.getAllInventory("company-1", null, null);

        // assert
        assertThat(result).hasSize(1);
        verify(inventoryRepository).findByProductCompanyId("company-1");
    }

    @Test
    void getAllInventory_withProductId_filtersByProduct() {
        // arrange
        when(inventoryRepository.findByProductId("prod-1")).thenReturn(List.of(new Inventory()));

        // act
        List<Inventory> result = inventoryService.getAllInventory(null, "prod-1", null);

        // assert
        verify(inventoryRepository).findByProductId("prod-1");
        assertThat(result).hasSize(1);
    }

    @Test
    void createInventory_validRequest_savesAndRegistersMovement() {
        // arrange
        InventoryCreationRequestDTO req = new InventoryCreationRequestDTO("prod-1", "wh-1", 50);
        Product product = new Product();
        product.setId("prod-1");
        Warehouse warehouse = new Warehouse();
        warehouse.setId("wh-1");

        when(productService.getProductById("prod-1", "company-1")).thenReturn(product);
        when(warehouseService.getWarehouseById("wh-1", "company-1")).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId("prod-1", "wh-1")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // act
        Inventory result = inventoryService.createInventory(req, "company-1");

        // assert
        assertThat(result.getStockAvailable()).isEqualTo(50);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void createInventory_stockAlreadyExists_throwsIllegalState() {
        // arrange
        InventoryCreationRequestDTO req = new InventoryCreationRequestDTO("prod-1", "wh-1", 10);
        Product product = new Product();
        product.setId("prod-1");
        Warehouse warehouse = new Warehouse();
        warehouse.setId("wh-1");

        when(productService.getProductById("prod-1", "company-1")).thenReturn(product);
        when(warehouseService.getWarehouseById("wh-1", "company-1")).thenReturn(warehouse);
        when(inventoryRepository.findByProductIdAndWarehouseId("prod-1", "wh-1")).thenReturn(Optional.of(new Inventory()));

        // act & assert
        assertThatThrownBy(() -> inventoryService.createInventory(req, "company-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void increaseStock_validQuantity_addsToAvailable() {
        // arrange
        Inventory inv = new Inventory();
        inv.setId("inv-1");
        inv.setStockAvailable(20);
        when(inventoryRepository.findById("inv-1")).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StockAdjustmentRequestDTO req = new StockAdjustmentRequestDTO();
        req.setQuantity(10);
        req.setReason("Recepción");

        // act
        Inventory result = inventoryService.increaseStock("inv-1", req);

        // assert
        assertThat(result.getStockAvailable()).isEqualTo(30);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void decreaseStock_sufficientStock_subtractsFromAvailable() {
        // arrange
        Inventory inv = new Inventory();
        inv.setId("inv-1");
        inv.setStockAvailable(30);
        when(inventoryRepository.findById("inv-1")).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        StockAdjustmentRequestDTO req = new StockAdjustmentRequestDTO();
        req.setQuantity(10);
        req.setReason("Venta");

        // act
        Inventory result = inventoryService.decreaseStock("inv-1", req);

        // assert
        assertThat(result.getStockAvailable()).isEqualTo(20);
    }

    @Test
    void decreaseStock_insufficientStock_throwsInsufficientStockException() {
        // arrange
        Inventory inv = new Inventory();
        inv.setId("inv-1");
        inv.setStockAvailable(5);
        when(inventoryRepository.findById("inv-1")).thenReturn(Optional.of(inv));

        StockAdjustmentRequestDTO req = new StockAdjustmentRequestDTO();
        req.setQuantity(10);
        req.setReason("Venta");

        // act & assert
        assertThatThrownBy(() -> inventoryService.decreaseStock("inv-1", req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("insuficiente");
    }

    @Test
    void decreaseStock_zeroQuantity_throwsIllegalArgument() {
        // arrange
        Inventory inv = new Inventory();
        inv.setId("inv-1");
        inv.setStockAvailable(10);
        when(inventoryRepository.findById("inv-1")).thenReturn(Optional.of(inv));

        StockAdjustmentRequestDTO req = new StockAdjustmentRequestDTO();
        req.setQuantity(0);

        // act & assert
        assertThatThrownBy(() -> inventoryService.decreaseStock("inv-1", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }
}
