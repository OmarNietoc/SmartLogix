package com.smartlogix.order.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {
    private String productId;
    private String warehouseId;
    private String productName;
    private Integer quantity;
}
