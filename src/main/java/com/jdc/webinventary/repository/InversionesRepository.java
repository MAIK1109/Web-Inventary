package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.InversionesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface InversionesRepository extends JpaRepository<InversionesEntity, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "usuario")
    List<InversionesEntity> findAllByOrderByFechaDesc();
    List<InversionesEntity> findByUsuario_IdUsuarioOrderByFechaDesc(Integer usuarioId);
    List<InversionesEntity> findByUsuario_IdUsuarioAndFechaBetweenOrderByFechaDesc(
            Integer usuarioId, LocalDate desde, LocalDate hasta);
    java.util.Optional<InversionesEntity> findByIdInversionAndUsuario_IdUsuario(Integer idInversion, Integer usuarioId);
    List<InversionesEntity> findByEstadoOrderByFechaDesc(Byte estado);

    @Modifying
    @Query("update InversionesEntity i set i.estado = :nuevoEstado, i.aprobadoPor = :aprobador " +
            "where i.idInversion = :id and i.estado = :pendiente")
    int decidirSiPendiente(@Param("id") Integer id, @Param("pendiente") Byte pendiente,
                           @Param("nuevoEstado") Byte nuevoEstado, @Param("aprobador") String aprobador);

    @Query("select coalesce(sum(i.monto), 0) from InversionesEntity i where i.estado = :estado")
    Long totalPorEstado(@Param("estado") Byte estado);

    @Query("select coalesce(sum(i.monto), 0) from InversionesEntity i " +
            "where i.estado = :estado and i.usuario.idUsuario = :usuarioId")
    Long totalUsuarioPorEstado(@Param("estado") Byte estado, @Param("usuarioId") Integer usuarioId);

    @Query("select i.usuario.idUsuario, sum(i.monto) from InversionesEntity i " +
            "where i.estado = :estado group by i.usuario.idUsuario")
    List<Object[]> totalesUsuariosPorEstado(@Param("estado") Byte estado);

    default Map<Integer, Long> totalesAprobadosPorUsuario(Byte estado) {
        return totalesUsuariosPorEstado(estado).stream().collect(Collectors.toMap(
                row -> (Integer) row[0], row -> ((Number) row[1]).longValue()));
    }
}
