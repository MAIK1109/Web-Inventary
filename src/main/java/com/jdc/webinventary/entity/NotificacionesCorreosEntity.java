package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "notificaciones_correos")
public class NotificacionesCorreosEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnotificacion_correo")
    private Integer idNotificacionCorreo;

    @Column(name = "destinatario", length = 45)
    private String destinatario;

    @Column(name = "tipo", length = 45)
    private String tipo;

    @Column(name = "estado", length = 45)
    private String estado;

    @Column(name = "intentos", length = 45)
    private String intentos;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Relación N:1 con VentasEntity (venta_idventa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_idventa", nullable = false)
    private VentasEntity venta;

    public NotificacionesCorreosEntity() {
    }

    public NotificacionesCorreosEntity(Integer idNotificacionCorreo, String destinatario, String tipo,
                                       String estado, String intentos, LocalDate fecha, VentasEntity venta) {
        this.idNotificacionCorreo = idNotificacionCorreo;
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.estado = estado;
        this.intentos = intentos;
        this.fecha = fecha;
        this.venta = venta;
    }

    public Integer getIdNotificacionCorreo() {
        return idNotificacionCorreo;
    }

    public void setIdNotificacionCorreo(Integer idNotificacionCorreo) {
        this.idNotificacionCorreo = idNotificacionCorreo;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIntentos() {
        return intentos;
    }

    public void setIntentos(String intentos) {
        this.intentos = intentos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    // Este getter y setter solucionan la falta de símbolo
    public VentasEntity getVenta() {
        return venta;
    }

    public void setVenta(VentasEntity venta) {
        this.venta = venta;
    }
}