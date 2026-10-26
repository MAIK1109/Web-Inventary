package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.UsuariosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Integer> {
    boolean existsByCorreoIgnoreCase(String correo);
}
