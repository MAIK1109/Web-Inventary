package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.CambioAlertaInventarioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CambioAlertaInventarioRepository extends JpaRepository<CambioAlertaInventarioEntity, Integer> {
    @EntityGraph(attributePaths = "producto")
    @Query("select c from CambioAlertaInventarioEntity c order by c.fecha desc, c.id desc")
    List<CambioAlertaInventarioEntity> findRecientes(Pageable pageable);
}
