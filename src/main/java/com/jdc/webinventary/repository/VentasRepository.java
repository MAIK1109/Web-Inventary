package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.VentasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;

public interface VentasRepository extends JpaRepository<VentasEntity, Integer> {
    @EntityGraph(attributePaths = {"cliente", "vendedor", "detalles", "detalles.producto"})
    List<VentasEntity> findByFechaBetweenOrderByFechaAsc(LocalDate desde, LocalDate hasta);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "detalles", "detalles.producto"})
    List<VentasEntity> findAllByOrderByFechaDesc();
}
