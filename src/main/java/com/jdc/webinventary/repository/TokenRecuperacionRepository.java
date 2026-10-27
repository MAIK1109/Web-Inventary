package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.TokenRecuperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacionEntity, Integer> {
    @Query("select t from TokenRecuperacionEntity t join fetch t.usuario " +
            "where t.tokenHash = :hash and t.consumidoEn is null and t.expiraEn > :ahora")
    Optional<TokenRecuperacionEntity> buscarVigente(@Param("hash") String hash,
                                                   @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("update TokenRecuperacionEntity t set t.consumidoEn = :ahora " +
            "where t.usuario.idUsuario = :usuarioId and t.consumidoEn is null")
    int invalidarActivosUsuario(@Param("usuarioId") Integer usuarioId, @Param("ahora") LocalDateTime ahora);

    @Modifying
    @Query("update TokenRecuperacionEntity t set t.consumidoEn = :ahora " +
            "where t.tokenHash = :hash and t.consumidoEn is null and t.expiraEn > :ahora")
    int consumirSiVigente(@Param("hash") String hash, @Param("ahora") LocalDateTime ahora);
}
