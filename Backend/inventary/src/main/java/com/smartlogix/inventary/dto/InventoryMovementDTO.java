package com.smartlogix.inventary.dto;
import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryMovementDTO { private String id; private String inventoryId; private String movementType; private Integer quantity; private String reason; private LocalDateTime createdAt; }
