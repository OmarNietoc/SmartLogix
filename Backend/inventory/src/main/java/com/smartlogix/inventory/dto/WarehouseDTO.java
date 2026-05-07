package com.smartlogix.inventory.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseDTO { private String id; private String companyId; private String name; private String locationAddress; private String type; private String status; }
