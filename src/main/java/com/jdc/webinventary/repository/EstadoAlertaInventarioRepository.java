package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.EstadoAlertaInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoAlertaInventarioRepository extends JpaRepository<EstadoAlertaInventarioEntity, Integer> {
    Optional<EstadoAlertaInventarioEntity> findByProducto_IdProducto(Integer productoId);
    boolean existsByProducto_IdProducto(Integer productoId);
}
