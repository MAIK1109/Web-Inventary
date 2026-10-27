package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.GastosEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GastosRepository extends JpaRepository<GastosEntity, Integer> {
    @EntityGraph(attributePaths = "usuario")
    List<GastosEntity> findByFechaBetweenOrderByFechaAsc(LocalDate desde, LocalDate hasta);
}
