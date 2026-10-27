package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.ComprasEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.time.LocalDate;

public interface ComprasRepository extends JpaRepository<ComprasEntity, Integer> {
    @EntityGraph(attributePaths = {"proveedor", "detalles", "detalles.producto"})
    List<ComprasEntity> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    List<ComprasEntity> findByFechaLessThanEqualAndEstadoOrderByFechaDesc(LocalDate fecha, Byte estado);

    @Query("select d.costoUnitario from DetalleComprasEntity d where d.producto.idProducto = :productoId " +
            "and d.compra.estado = :estado and d.compra.fecha <= :fecha and d.costoUnitario is not null " +
            "order by d.compra.fecha desc, d.idDetalleCompra desc")
    List<Integer> costosProductoHasta(@Param("productoId") Integer productoId,
                                     @Param("fecha") LocalDate fecha, @Param("estado") Byte estado,
                                     Pageable pageable);

    default Integer costoUnitarioMasReciente(Integer productoId, LocalDate fecha) {
        List<Integer> resultados = costosProductoHasta(productoId, fecha, (byte) 1, Pageable.ofSize(1));
        return resultados.isEmpty() ? null : resultados.get(0);
    }
}
