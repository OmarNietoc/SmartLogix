package com.smartlogix.inventory.service;

import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.exception.WarehouseNotFoundException;
import com.smartlogix.inventory.model.Warehouse;
import com.smartlogix.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock private WarehouseRepository warehouseRepository;
    @InjectMocks private WarehouseService warehouseService;

    private static final String COMPANY_ID = "company-1";

    // ── getAllWarehouses ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllWarehouses without type filter returns all warehouses")
    void getAllWarehouses_noFilter_returnsAll() {
        when(warehouseRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(buildWarehouse("w1"), buildWarehouse("w2")));

        List<Warehouse> result = warehouseService.getAllWarehouses(COMPANY_ID, null);

        assertThat(result).hasSize(2);
        verify(warehouseRepository).findByCompanyId(COMPANY_ID);
        verify(warehouseRepository, never()).findByCompanyIdAndType(any(), any());
    }

    @Test
    @DisplayName("getAllWarehouses with type filter delegates to filtered query")
    void getAllWarehouses_withTypeFilter_callsFilteredQuery() {
        when(warehouseRepository.findByCompanyIdAndType(COMPANY_ID, WarehouseType.WAREHOUSE))
                .thenReturn(List.of(buildWarehouse("w1")));

        List<Warehouse> result = warehouseService.getAllWarehouses(COMPANY_ID, WarehouseType.WAREHOUSE);

        assertThat(result).hasSize(1);
        verify(warehouseRepository).findByCompanyIdAndType(COMPANY_ID, WarehouseType.WAREHOUSE);
        verify(warehouseRepository, never()).findByCompanyId(any());
    }

    @Test
    @DisplayName("getAllWarehouses throws when companyId is blank")
    void getAllWarehouses_blankCompanyId_throws() {
        assertThatThrownBy(() -> warehouseService.getAllWarehouses("", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
    }

    // ── getWarehouseById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getWarehouseById found returns warehouse")
    void getWarehouseById_found_returnsWarehouse() {
        Warehouse warehouse = buildWarehouse("w1");
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(warehouse));

        Warehouse result = warehouseService.getWarehouseById("w1", COMPANY_ID);

        assertThat(result.getId()).isEqualTo("w1");
    }

    @Test
    @DisplayName("getWarehouseById not found throws WarehouseNotFoundException")
    void getWarehouseById_notFound_throws() {
        when(warehouseRepository.findByIdAndCompanyId("bad", COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById("bad", COMPANY_ID))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("bad");
    }

    // ── createWarehouse ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createWarehouse saves and returns warehouse")
    void createWarehouse_valid_savesAndReturns() {
        Warehouse warehouse = buildWarehouse(null);
        when(warehouseRepository.save(any())).thenReturn(warehouse);

        Warehouse result = warehouseService.createWarehouse(warehouse, COMPANY_ID);

        assertThat(result.getCompanyId()).isEqualTo(COMPANY_ID);
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    @DisplayName("createWarehouse throws when name is blank")
    void createWarehouse_blankName_throws() {
        Warehouse warehouse = buildWarehouse(null);
        warehouse.setName("");

        assertThatThrownBy(() -> warehouseService.createWarehouse(warehouse, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("createWarehouse throws when locationAddress is blank")
    void createWarehouse_blankAddress_throws() {
        Warehouse warehouse = buildWarehouse(null);
        warehouse.setLocationAddress("");

        assertThatThrownBy(() -> warehouseService.createWarehouse(warehouse, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locationAddress");
    }

    @Test
    @DisplayName("createWarehouse throws when type is null")
    void createWarehouse_nullType_throws() {
        Warehouse warehouse = buildWarehouse(null);
        warehouse.setType(null);

        assertThatThrownBy(() -> warehouseService.createWarehouse(warehouse, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    // ── updateWarehouse ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateWarehouse updates fields and saves")
    void updateWarehouse_valid_updatesFields() {
        Warehouse existing = buildWarehouse("w1");
        Warehouse updates = buildWarehouse("w1");
        updates.setName("Bodega Nueva");
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(existing)).thenReturn(existing);

        Warehouse result = warehouseService.updateWarehouse("w1", updates, COMPANY_ID);

        assertThat(result.getName()).isEqualTo("Bodega Nueva");
        verify(warehouseRepository).save(existing);
    }

    // ── deleteWarehouse ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteWarehouse sets status INACTIVE (soft delete)")
    void deleteWarehouse_existing_setsInactive() {
        Warehouse warehouse = buildWarehouse("w1");
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(warehouse)).thenReturn(warehouse);

        warehouseService.deleteWarehouse("w1", COMPANY_ID);

        assertThat(warehouse.getStatus()).isEqualTo("INACTIVE");
        verify(warehouseRepository).save(warehouse);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Warehouse buildWarehouse(String id) {
        return Warehouse.builder()
                .id(id)
                .companyId(COMPANY_ID)
                .name("Bodega Principal")
                .locationAddress("Av. Industrial 123")
                .type(WarehouseType.WAREHOUSE)
                .build();
    }
}
