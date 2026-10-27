package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "inversiones")
public class InversionesEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idinversion")
    private Integer idInversion;

    @Column(name = "monto")
    private Integer monto;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "medio_pago", length = 45)
    private String medioPago;

    @Column(name = "estado")
    private Byte estado;

    @Column(name = "aprobado_por", length = 45)
    private String aprobadoPor;

    @Column(name = "soporte_archivo", length = 100)
    private String soporteArchivo;

    // Relación Muchos a Uno con UsuariosEntity (Usuario_idUsuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_idUsuario", nullable = false)
    private UsuariosEntity usuario;

    public InversionesEntity() {
    }

    public InversionesEntity(Integer idInversion, Integer monto, LocalDate fecha,
                             String medioPago, Byte estado, String aprobadoPor,
                             UsuariosEntity usuario) {
        this.idInversion = idInversion;
        this.monto = monto;
        this.fecha = fecha;
        this.medioPago = medioPago;
        this.estado = estado;
        this.aprobadoPor = aprobadoPor;
        this.usuario = usuario;
    }

    public Integer getIdInversion() {
        return idInversion;
    }

    public void setIdInversion(Integer idInversion) {
        this.idInversion = idInversion;
    }

    public Integer getMonto() {
        return monto;
    }

    public void setMonto(Integer monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public Byte getEstado() {
        return estado;
    }

    public void setEstado(Byte estado) {
        this.estado = estado;
    }

    public String getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(String aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public String getSoporteArchivo() { return soporteArchivo; }
    public void setSoporteArchivo(String soporteArchivo) { this.soporteArchivo = soporteArchivo; }

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }
}
