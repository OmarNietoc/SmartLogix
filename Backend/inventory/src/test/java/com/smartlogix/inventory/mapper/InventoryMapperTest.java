package com.smartlogix.inventory.mapper;

import com.smartlogix.inventory.dto.InventoryDTO;
import com.smartlogix.inventory.dto.ProductDTO;
import com.smartlogix.inventory.dto.WarehouseDTO;
import com.smartlogix.inventory.enums.MovementType;
import com.smartlogix.inventory.enums.ReservationStatus;
import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.model.Inventory;
import com.smartlogix.inventory.model.InventoryMovement;
import com.smartlogix.inventory.model.InventoryReservation;
import com.smartlogix.inventory.model.Product;
import com.smartlogix.inventory.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryMapperTest {

    private final ProductMapper productMapper = new ProductMapperImpl();
    private final WarehouseMapper warehouseMapper = new WarehouseMapperImpl();
    private final InventoryMapper inventoryMapper = new InventoryMapperImpl();
    private final InventoryMovementMapper movementMapper = new InventoryMovementMapperImpl();
    private final InventoryReservationMapper reservationMapper = new InventoryReservationMapperImpl();

    @Test
    void productMapper_mapsProductAndKeepsEntityDefaultStatusWhenDtoIsConverted() {
        Product product = product();

        ProductDTO dto = productMapper.toDto(product);
        Product entity = productMapper.toEntity(dto);

        assertThat(dto.getId()).isEqualTo("product-1");
        assertThat(dto.getCompanyId()).isEqualTo("company-1");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getSku()).isEqualTo("SKU-1");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void warehouseMapper_mapsWarehouseAndIgnoresStatusOnEntityInput() {
        Warehouse warehouse = warehouse();

        WarehouseDTO dto = warehouseMapper.toDto(warehouse);
        Warehouse entity = warehouseMapper.toEntity(dto);

        assertThat(dto.getType()).isEqualTo("WAREHOUSE");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getType()).isEqualTo(WarehouseType.WAREHOUSE);
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void inventoryMapper_flattensProductAndWarehouseFields() {
        LocalDateTime now = LocalDateTime.now();
        Inventory inventory = Inventory.builder()
                .id("stock-1")
                .product(product())
                .warehouse(warehouse())
                .stockAvailable(20)
                .stockReserved(3)
                .lastUpdated(now)
                .build();

        InventoryDTO dto = inventoryMapper.toDto(inventory);
        Inventory entity = inventoryMapper.toEntity(dto);

        assertThat(dto.getProductId()).isEqualTo("product-1");
        assertThat(dto.getWarehouseId()).isEqualTo("warehouse-1");
        assertThat(dto.getSku()).isEqualTo("SKU-1");
        assertThat(dto.getWarehouseName()).isEqualTo("Bodega Central");
        assertThat(entity.getProduct()).isNull();
        assertThat(entity.getWarehouse()).isNull();
        assertThat(entity.getStockAvailable()).isEqualTo(20);
    }

    @Test
    void inventoryMapper_handlesNullNestedObjects() {
        InventoryDTO dto = inventoryMapper.toDto(Inventory.builder()
                .id("stock-1")
                .stockAvailable(1)
                .stockReserved(0)
                .build());

        assertThat(dto.getProductId()).isNull();
        assertThat(dto.getWarehouseId()).isNull();
        assertThat(dto.getSku()).isNull();
    }

    @Test
    void movementMapper_mapsInventoryIdAndMovementType() {
        InventoryMovement movement = InventoryMovement.builder()
                .id("movement-1")
                .inventory(Inventory.builder().id("stock-1").build())
                .movementType(MovementType.IN)
                .quantity(5)
                .reason("Ingreso")
                .createdAt(LocalDateTime.now())
                .build();

        var dto = movementMapper.toDto(movement);

        assertThat(dto.getInventoryId()).isEqualTo("stock-1");
        assertThat(dto.getMovementType()).isEqualTo("IN");
        assertThat(dto.getQuantity()).isEqualTo(5);
    }

    @Test
    void reservationMapper_mapsProductWarehouseAndStatus() {
        InventoryReservation reservation = InventoryReservation.builder()
                .id("reservation-1")
                .orderId("order-1")
                .product(product())
                .warehouse(warehouse())
                .quantity(2)
                .status(ReservationStatus.RESERVED)
                .createdAt(LocalDateTime.now())
                .build();

        var dto = reservationMapper.toDto(reservation);

        assertThat(dto.getOrderId()).isEqualTo("order-1");
        assertThat(dto.getProductId()).isEqualTo("product-1");
        assertThat(dto.getWarehouseId()).isEqualTo("warehouse-1");
        assertThat(dto.getStatus()).isEqualTo("RESERVED");
    }

    @Test
    void mappersReturnNullForNullInput() {
        assertThat(productMapper.toDto(null)).isNull();
        assertThat(productMapper.toEntity(null)).isNull();
        assertThat(warehouseMapper.toDto(null)).isNull();
        assertThat(warehouseMapper.toEntity(null)).isNull();
        assertThat(inventoryMapper.toDto(null)).isNull();
        assertThat(inventoryMapper.toEntity(null)).isNull();
        assertThat(movementMapper.toDto(null)).isNull();
        assertThat(reservationMapper.toDto(null)).isNull();
    }

    private Product product() {
        return Product.builder()
                .id("product-1")
                .companyId("company-1")
                .sku("SKU-1")
                .name("Producto Demo")
                .price(BigDecimal.TEN)
                .status("ACTIVE")
                .build();
    }

    private Warehouse warehouse() {
        return Warehouse.builder()
                .id("warehouse-1")
                .companyId("company-1")
                .name("Bodega Central")
                .locationAddress("Av. Demo 123")
                .type(WarehouseType.WAREHOUSE)
                .status("ACTIVE")
                .build();
    }
}
