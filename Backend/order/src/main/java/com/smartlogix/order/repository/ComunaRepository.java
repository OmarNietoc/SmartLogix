package com.smartlogix.order.repository;

import com.smartlogix.order.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunaRepository extends JpaRepository<Comuna, Integer> {
    List<Comuna> findByRegionId(Integer regionId);
}
