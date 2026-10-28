package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.VentasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface VentasRepository extends JpaRepository<VentasEntity, Integer> {
    boolean existsByCliente_IdCliente(Integer clienteId);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "detalles", "detalles.producto"})
    List<VentasEntity> findByFechaBetweenOrderByFechaAsc(LocalDate desde, LocalDate hasta);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "detalles", "detalles.producto"})
    List<VentasEntity> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"cliente", "vendedor", "detalles", "detalles.producto"})
    @Query("select v from VentasEntity v where v.idVenta = :id")
    Optional<VentasEntity> buscarParaAnular(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<VentasEntity> findByVendedor_IdUsuarioOrderByFechaDesc(Integer usuarioId);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<VentasEntity> findByVendedor_IdUsuarioAndFechaBetweenOrderByFechaDesc(
            Integer usuarioId, LocalDate desde, LocalDate hasta);
}
