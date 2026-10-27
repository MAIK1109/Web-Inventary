package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class VentasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idventa")
    private Integer idVenta;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "estado")
    private Byte estado;

    // Relación N:1 con ClientesEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_idcliente", nullable = false)
    private ClientesEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_idusuario")
    private UsuariosEntity vendedor;

    // Relación 1:N con DetalleVentasEntity
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVentasEntity> detalles = new ArrayList<>();

    // Relación 1:N con NotificacionesCorreosEntity
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificacionesCorreosEntity> notificaciones = new ArrayList<>();

    public VentasEntity() {
    }

    public VentasEntity(Integer idVenta, LocalDate fecha, Byte estado, ClientesEntity cliente) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.estado = estado;
        this.cliente = cliente;
    }

    // Métodos de conveniencia para la relación bidireccional con DetalleVentasEntity
    public void addDetalle(DetalleVentasEntity detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void removeDetalle(DetalleVentasEntity detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }

    // Métodos de conveniencia para la relación bidireccional con NotificacionesCorreosEntity
    public void addNotificacion(NotificacionesCorreosEntity notificacion) {
        notificaciones.add(notificacion);
        notificacion.setVenta(this);
    }

    public void removeNotificacion(NotificacionesCorreosEntity notificacion) {
        notificaciones.remove(notificacion);
        notificacion.setVenta(null);
    }

    // Getters y Setters
    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Byte getEstado() {
        return estado;
    }

    public void setEstado(Byte estado) {
        this.estado = estado;
    }

    public ClientesEntity getCliente() {
        return cliente;
    }

    public void setCliente(ClientesEntity cliente) {
        this.cliente = cliente;
    }

    public UsuariosEntity getVendedor() { return vendedor; }

    public void setVendedor(UsuariosEntity vendedor) { this.vendedor = vendedor; }

    public List<DetalleVentasEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentasEntity> detalles) {
        this.detalles = detalles;
    }

    public List<NotificacionesCorreosEntity> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<NotificacionesCorreosEntity> notificaciones) {
        this.notificaciones = notificaciones;
    }
}
