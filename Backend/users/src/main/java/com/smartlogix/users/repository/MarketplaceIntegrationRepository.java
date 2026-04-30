package com.smartlogix.users.repository;

import com.smartlogix.users.model.MarketplaceIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceIntegrationRepository extends JpaRepository<MarketplaceIntegration, String> {
    List<MarketplaceIntegration> findByCompanyId(String companyId);
}
