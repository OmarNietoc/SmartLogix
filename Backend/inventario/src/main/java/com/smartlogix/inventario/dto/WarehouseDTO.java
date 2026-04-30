package com.smartlogix.inventario.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseDTO { private String id; private String companyId; private String name; private String locationAddress; private String type; }
