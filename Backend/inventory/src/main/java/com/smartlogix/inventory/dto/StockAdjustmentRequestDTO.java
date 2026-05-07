package com.smartlogix.inventory.dto;
import lombok.*;
/** quantity se envía positivo; el endpoint define si aumenta o descuenta. */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockAdjustmentRequestDTO { private Integer quantity; private String reason; }
