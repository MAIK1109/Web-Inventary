package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.UsuariosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuariosRepository extends JpaRepository<UsuariosEntity, Integer> {
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, Integer idUsuario);
    Optional<UsuariosEntity> findByCorreoIgnoreCase(String correo);
}
