package com.smartlogix.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String id;
    private String authId;
    private String companyId;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}
