package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.ClientesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientesRepository extends JpaRepository<ClientesEntity, Integer> {
    boolean existsByIdentificacionIgnoreCase(String identificacion);
}
