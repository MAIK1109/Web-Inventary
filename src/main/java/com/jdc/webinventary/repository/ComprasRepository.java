package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.ComprasEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprasRepository extends JpaRepository<ComprasEntity, Integer> {
    @EntityGraph(attributePaths = {"proveedor", "detalles", "detalles.producto"})
    List<ComprasEntity> findAllByOrderByFechaDesc();
}
