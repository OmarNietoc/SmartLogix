package com.smartlogix.inventory.service;

import com.smartlogix.inventory.enums.WarehouseType;
import com.smartlogix.inventory.exception.WarehouseNotFoundException;
import com.smartlogix.inventory.model.Warehouse;
import com.smartlogix.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock private WarehouseRepository warehouseRepository;
    @InjectMocks private WarehouseService warehouseService;

    @Test
    void getAllWarehouses_withoutType_usesCompanyFilter() {
        when(warehouseRepository.findByCompanyId("company-1")).thenReturn(List.of(warehouse("w1")));

        List<Warehouse> result = warehouseService.getAllWarehouses("company-1", null);

        assertThat(result).hasSize(1);
        verify(warehouseRepository).findByCompanyId("company-1");
    }

    @Test
    void getAllWarehouses_withType_usesCompanyAndTypeFilter() {
        when(warehouseRepository.findByCompanyIdAndType("company-1", WarehouseType.RETAIL_STORE))
                .thenReturn(List.of(warehouse("w1")));

        List<Warehouse> result = warehouseService.getAllWarehouses("company-1", WarehouseType.RETAIL_STORE);

        assertThat(result).hasSize(1);
        verify(warehouseRepository).findByCompanyIdAndType("company-1", WarehouseType.RETAIL_STORE);
    }

    @Test
    void getWarehouseById_returnsWarehouseOrThrowsNotFound() {
        when(warehouseRepository.findByIdAndCompanyId("w1", "company-1")).thenReturn(Optional.of(warehouse("w1")));

        Warehouse result = warehouseService.getWarehouseById("w1", "company-1");

        assertThat(result.getId()).isEqualTo("w1");

        when(warehouseRepository.findByIdAndCompanyId("missing", "company-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> warehouseService.getWarehouseById("missing", "company-1"))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void createWarehouse_usesAuthenticatedCompanyAndValidatesRequiredFields() {
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        Warehouse saved = warehouseService.createWarehouse(warehouse("new"), "company-1");

        assertThat(saved.getCompanyId()).isEqualTo("company-1");
        verify(warehouseRepository).save(saved);

        assertThatThrownBy(() -> warehouseService.createWarehouse(warehouse("bad"), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X-Company-Id");

        Warehouse invalidName = warehouse("bad");
        invalidName.setName(" ");
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidName, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");

        Warehouse invalidAddress = warehouse("bad");
        invalidAddress.setLocationAddress(" ");
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidAddress, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locationAddress");

        Warehouse invalidType = warehouse("bad");
        invalidType.setType(null);
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidType, "company-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void updateWarehouse_updatesMutableFields() {
        Warehouse existing = warehouse("w1");
        Warehouse update = warehouse("ignored");
        update.setName("Nueva bodega");
        update.setLocationAddress("Nueva direccion");
        update.setType(WarehouseType.RETAIL_STORE);
        when(warehouseRepository.findByIdAndCompanyId("w1", "company-1")).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        Warehouse result = warehouseService.updateWarehouse("w1", update, "company-1");

        assertThat(result.getName()).isEqualTo("Nueva bodega");
        assertThat(result.getLocationAddress()).isEqualTo("Nueva direccion");
        assertThat(result.getType()).isEqualTo(WarehouseType.RETAIL_STORE);
    }

    @Test
    void deleteWarehouse_marksInactive() {
        Warehouse existing = warehouse("w1");
        when(warehouseRepository.findByIdAndCompanyId("w1", "company-1")).thenReturn(Optional.of(existing));

        warehouseService.deleteWarehouse("w1", "company-1");

        assertThat(existing.getStatus()).isEqualTo("INACTIVE");
        verify(warehouseRepository).save(existing);
    }

    private Warehouse warehouse(String id) {
        return Warehouse.builder()
                .id(id)
                .companyId("company-1")
                .name("Bodega " + id)
                .locationAddress("Av. Demo 123")
                .type(WarehouseType.WAREHOUSE)
                .status("ACTIVE")
                .build();
    }
}
