package com.smartlogix.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private String id;
    private String taxId;
    private String name;
    private String contactEmail;
    private String phone;
}
