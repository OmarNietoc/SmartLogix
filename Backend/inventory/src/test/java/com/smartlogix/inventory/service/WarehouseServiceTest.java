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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    private static final String COMPANY_ID = "company-1";

    @Mock private WarehouseRepository warehouseRepository;
    @InjectMocks private WarehouseService warehouseService;

    @Test
    @DisplayName("getAllWarehouses without type filter returns company warehouses")
    void getAllWarehouses_withoutType_usesCompanyFilter() {
        when(warehouseRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(warehouse("w1"), warehouse("w2")));

        List<Warehouse> result = warehouseService.getAllWarehouses(COMPANY_ID, null);

        assertThat(result).hasSize(2);
        verify(warehouseRepository).findByCompanyId(COMPANY_ID);
        verify(warehouseRepository, never()).findByCompanyIdAndType(any(), any());
    }

    @Test
    @DisplayName("getAllWarehouses with type filter delegates to filtered query")
    void getAllWarehouses_withType_usesCompanyAndTypeFilter() {
        when(warehouseRepository.findByCompanyIdAndType(COMPANY_ID, WarehouseType.RETAIL_STORE))
                .thenReturn(List.of(warehouse("w1")));

        List<Warehouse> result = warehouseService.getAllWarehouses(COMPANY_ID, WarehouseType.RETAIL_STORE);

        assertThat(result).hasSize(1);
        verify(warehouseRepository).findByCompanyIdAndType(COMPANY_ID, WarehouseType.RETAIL_STORE);
        verify(warehouseRepository, never()).findByCompanyId(any());
    }

    @Test
    @DisplayName("getAllWarehouses throws when companyId is blank")
    void getAllWarehouses_blankCompanyId_throws() {
        assertThatThrownBy(() -> warehouseService.getAllWarehouses(" ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    @DisplayName("getWarehouseById returns warehouse when found")
    void getWarehouseById_found_returnsWarehouse() {
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(warehouse("w1")));

        Warehouse result = warehouseService.getWarehouseById("w1", COMPANY_ID);

        assertThat(result.getId()).isEqualTo("w1");
    }

    @Test
    @DisplayName("getWarehouseById throws when warehouse does not exist for company")
    void getWarehouseById_notFound_throwsWarehouseNotFound() {
        when(warehouseRepository.findByIdAndCompanyId("missing", COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById("missing", COMPANY_ID))
                .isInstanceOf(WarehouseNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("createWarehouse saves with authenticated company")
    void createWarehouse_valid_usesAuthenticatedCompanyAndSaves() {
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(inv -> inv.getArgument(0));

        Warehouse saved = warehouseService.createWarehouse(warehouse("new"), COMPANY_ID);

        assertThat(saved.getCompanyId()).isEqualTo(COMPANY_ID);
        verify(warehouseRepository).save(saved);
    }

    @Test
    @DisplayName("createWarehouse validates required company and warehouse fields")
    void createWarehouse_invalidRequiredFields_throws() {
        assertThatThrownBy(() -> warehouseService.createWarehouse(warehouse("bad"), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X-Company-Id");

        Warehouse invalidName = warehouse("bad");
        invalidName.setName(" ");
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidName, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");

        Warehouse invalidAddress = warehouse("bad");
        invalidAddress.setLocationAddress(" ");
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidAddress, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locationAddress");

        Warehouse invalidType = warehouse("bad");
        invalidType.setType(null);
        assertThatThrownBy(() -> warehouseService.createWarehouse(invalidType, COMPANY_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    @DisplayName("updateWarehouse updates mutable fields and saves")
    void updateWarehouse_updatesMutableFields() {
        Warehouse existing = warehouse("w1");
        Warehouse update = warehouse("ignored");
        update.setName("Nueva bodega");
        update.setLocationAddress("Nueva direccion");
        update.setType(WarehouseType.RETAIL_STORE);
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(existing)).thenReturn(existing);

        Warehouse result = warehouseService.updateWarehouse("w1", update, COMPANY_ID);

        assertThat(result.getName()).isEqualTo("Nueva bodega");
        assertThat(result.getLocationAddress()).isEqualTo("Nueva direccion");
        assertThat(result.getType()).isEqualTo(WarehouseType.RETAIL_STORE);
        verify(warehouseRepository).save(existing);
    }

    @Test
    @DisplayName("deleteWarehouse marks warehouse inactive")
    void deleteWarehouse_marksInactive() {
        Warehouse existing = warehouse("w1");
        when(warehouseRepository.findByIdAndCompanyId("w1", COMPANY_ID)).thenReturn(Optional.of(existing));
        when(warehouseRepository.save(existing)).thenReturn(existing);

        warehouseService.deleteWarehouse("w1", COMPANY_ID);

        assertThat(existing.getStatus()).isEqualTo("INACTIVE");
        verify(warehouseRepository).save(existing);
    }

    private Warehouse warehouse(String id) {
        return Warehouse.builder()
                .id(id)
                .companyId(COMPANY_ID)
                .name("Bodega " + id)
                .locationAddress("Av. Demo 123")
                .type(WarehouseType.WAREHOUSE)
                .status("ACTIVE")
                .build();
    }
}
