package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.AnulacionesEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnulacionesRepository extends JpaRepository<AnulacionesEntity, Integer> {
    boolean existsByTipoOperacionAndOperacionId(String tipoOperacion, Integer operacionId);

    @EntityGraph(attributePaths = "usuario")
    List<AnulacionesEntity> findAllByTipoOperacionOrderByFechaAnulacionDesc(String tipoOperacion);
}
