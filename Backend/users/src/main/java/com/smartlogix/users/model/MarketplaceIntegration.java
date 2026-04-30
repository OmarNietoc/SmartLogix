package com.smartlogix.users.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marketplace_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketplaceIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "platform_name", nullable = false)
    private String platformName;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
