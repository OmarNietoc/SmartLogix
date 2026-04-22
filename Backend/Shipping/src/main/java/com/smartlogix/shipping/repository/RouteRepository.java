package com.smartlogix.shipping.repository;

import com.smartlogix.shipping.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {
    List<Route> findByCompanyId(String companyId);
}
