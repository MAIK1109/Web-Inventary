package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "movimientos_inventarios")
public class MovimientosInventariosEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idmovimiento_inventario")
    private Integer idMovimientoInventario;

    @Column(name = "tipo", length = 45)
    private String tipo;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "documento_origen", length = 45)
    private String documentoOrigen;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Relación N:1 con UsuariosEntity (Usuario_idUsuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Usuario_idUsuario", nullable = false)
    private UsuariosEntity usuario;

    // Relación N:1 con ProductosEntity (producto_idproducto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_idproducto", nullable = false)
    private ProductosEntity producto;

    public MovimientosInventariosEntity() {
    }

    public MovimientosInventariosEntity(Integer idMovimientoInventario, String tipo, Integer cantidad,
                                        String documentoOrigen, LocalDate fecha,
                                        UsuariosEntity usuario, ProductosEntity producto) {
        this.idMovimientoInventario = idMovimientoInventario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.documentoOrigen = documentoOrigen;
        this.fecha = fecha;
        this.usuario = usuario;
        this.producto = producto;
    }

    public Integer getIdMovimientoInventario() {
        return idMovimientoInventario;
    }

    public void setIdMovimientoInventario(Integer idMovimientoInventario) {
        this.idMovimientoInventario = idMovimientoInventario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getDocumentoOrigen() {
        return documentoOrigen;
    }

    public void setDocumentoOrigen(String documentoOrigen) {
        this.documentoOrigen = documentoOrigen;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public UsuariosEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosEntity usuario) {
        this.usuario = usuario;
    }

    public ProductosEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductosEntity producto) {
        this.producto = producto;
    }
}
