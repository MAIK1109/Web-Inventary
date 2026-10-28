package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.MovimientosInventariosEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovimientosInventariosRepository extends JpaRepository<MovimientosInventariosEntity, Integer> {
    boolean existsByProducto_IdProducto(Integer productoId);

    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientosInventariosEntity> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"producto", "usuario"})
    @Query("select m from MovimientosInventariosEntity m order by m.fecha desc, m.idMovimientoInventario desc")
    List<MovimientosInventariosEntity> findRecientes(Pageable pageable);
}
