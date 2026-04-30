package com.smartlogix.inventario.dto;
import lombok.*; import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO { private String id; private String companyId; private String sku; private String name; private BigDecimal price; }
