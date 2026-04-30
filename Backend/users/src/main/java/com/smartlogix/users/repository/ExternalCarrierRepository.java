package com.smartlogix.users.repository;

import com.smartlogix.users.model.ExternalCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalCarrierRepository extends JpaRepository<ExternalCarrier, String> {
    List<ExternalCarrier> findByCompanyId(String companyId);
}
