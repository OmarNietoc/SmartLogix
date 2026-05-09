package com.smartlogix.auth_service.dto;
import lombok.Data;
@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String companyName;
    private String taxId;
    private String firstName;
    private String lastName;
    private String contactEmail;
    private String phone;
}
