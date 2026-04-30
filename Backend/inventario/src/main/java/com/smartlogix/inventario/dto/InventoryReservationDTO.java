package com.smartlogix.inventario.dto;
import lombok.*; import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryReservationDTO { private String id; private String orderId; private String productId; private String warehouseId; private Integer quantity; private String status; private LocalDateTime createdAt; }
