package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "auditorias")
public class AuditoriasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idauditoria")
    private Integer idAuditoria;

    @Column(name = "accion", length = 45)
    private String accion;

    @Column(name = "entidad", length = 45)
    private String entidad;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Relación N:1 con UsuariosEntity (Usuario_idUsuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_idUsuario", nullable = false)
    private UsuariosEntity usuario;

    public AuditoriasEntity() {
    }

    public AuditoriasEntity(Integer idAuditoria, String accion, String entidad, LocalDate fecha, UsuariosEntity usuario) {
        this.idAuditoria = idAuditoria;
        this.accion = accion;
        this.entidad = entidad;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    // Métodos getter y setter para la relación con UsuariosEntity
    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }
}