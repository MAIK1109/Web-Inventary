package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anulaciones", uniqueConstraints = {
        @UniqueConstraint(name = "uk_anulacion_operacion", columnNames = {"tipo_operacion", "operacion_id"})
})
public class AnulacionesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idanulacion")
    private Integer idAnulacion;

    @Column(name = "tipo_operacion", nullable = false, length = 30)
    private String tipoOperacion;

    @Column(name = "operacion_id", nullable = false)
    private Integer operacionId;

    @Column(name = "fecha_anulacion", nullable = false)
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo", nullable = false, length = 250)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_idusuario", nullable = false)
    private UsuariosEntity usuario;

    protected AnulacionesEntity() { }

    public AnulacionesEntity(String tipoOperacion, Integer operacionId, LocalDateTime fechaAnulacion,
                             String motivo, UsuariosEntity usuario) {
        this.tipoOperacion = tipoOperacion;
        this.operacionId = operacionId;
        this.fechaAnulacion = fechaAnulacion;
        this.motivo = motivo;
        this.usuario = usuario;
    }

    public Integer getIdAnulacion() { return idAnulacion; }
    public String getTipoOperacion() { return tipoOperacion; }
    public Integer getOperacionId() { return operacionId; }
    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public String getMotivo() { return motivo; }
    public UsuariosEntity getUsuario() { return usuario; }
}
