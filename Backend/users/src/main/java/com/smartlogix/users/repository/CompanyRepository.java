package com.smartlogix.users.repository;

import com.smartlogix.users.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);

    @Query("select count(c) > 0 from Company c where upper(replace(replace(c.taxId, '.', ''), '-', '')) = :taxId")
    boolean existsByNormalizedTaxId(@Param("taxId") String taxId);
}
