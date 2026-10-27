package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.ProveedoresEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedoresRepository extends JpaRepository<ProveedoresEntity, Integer> {
    boolean existsByIdentificacionIgnoreCase(String identificacion);
}
