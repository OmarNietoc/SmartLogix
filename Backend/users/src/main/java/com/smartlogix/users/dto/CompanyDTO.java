package com.smartlogix.users.dto;

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
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]{8}[0-9Kk]$", message = "El RUT debe tener 9 caracteres, terminando en número o K")
    private String taxId;
    private String name;
    private String contactEmail;
    private String phone;
}
