package com.smartlogix.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceIntegrationDTO {
    private String id;
    private String companyId;
    private String platformName;
    private String webhookSecret;
    private boolean isActive;
}
