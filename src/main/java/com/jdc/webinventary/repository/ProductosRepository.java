package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.ProductosEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Integer>, JpaSpecificationExecutor<ProductosEntity> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdProductoNot(String codigo, Integer idProducto);

    List<ProductosEntity> findByEstadoIsNullOrEstado(Byte estado, Sort sort);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductosEntity p where p.idProducto = :id")
    Optional<ProductosEntity> findByIdForUpdate(@Param("id") Integer id);

    @Modifying
    @Query("update ProductosEntity p set p.stockActual = p.stockActual - :cantidad " +
            "where p.idProducto = :id and p.stockActual >= :cantidad")
    int descontarStockSiDisponible(@Param("id") Integer id, @Param("cantidad") int cantidad);
}
