package com.smartlogix.inventario.dto;
import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryDTO { private String id; private String productId; private String warehouseId; private String sku; private String productName; private String warehouseName; private Integer stockAvailable; private Integer stockReserved; private LocalDateTime lastUpdated; }
