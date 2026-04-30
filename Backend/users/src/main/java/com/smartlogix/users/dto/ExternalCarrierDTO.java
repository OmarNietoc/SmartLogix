package com.smartlogix.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCarrierDTO {
    private String id;
    private String companyId;
    private String name;
    private String contactEmail;
    private String phone;
}
