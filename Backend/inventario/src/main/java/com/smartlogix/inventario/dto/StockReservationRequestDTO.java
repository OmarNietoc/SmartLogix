package com.smartlogix.inventario.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockReservationRequestDTO { private String orderId; private String productId; private String warehouseId; private Integer quantity; }
