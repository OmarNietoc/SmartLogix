package com.smartlogix.inventary.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryCreationRequestDTO { private String productId; private String warehouseId; private Integer stockAvailable; }
