package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.AuditoriasEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditoriasRepository extends JpaRepository<AuditoriasEntity, Integer> {
    @EntityGraph(attributePaths = "usuario")
    @Query("select a from AuditoriasEntity a order by a.fecha desc, a.idAuditoria desc")
    List<AuditoriasEntity> findRecientes(Pageable pageable);
}
