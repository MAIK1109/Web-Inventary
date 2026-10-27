package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "detalles_compras")
public class DetalleComprasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle_compra")
    private Integer idDetalleCompra;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "costo_unitario")
    private Integer costoUnitario;

    // Relación Mucho a Uno con CompraEntity (compra_idcompra)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_idcompra", nullable = false)
    private ComprasEntity compra;

    // Relación N:1 con ProductosEntity (producto_idproducto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_idproducto", nullable = false)
    private ProductosEntity producto;

    public DetalleComprasEntity() {
    }

    public Integer getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(Integer idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(Integer costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public ComprasEntity getCompra() {
        return compra;
    }

    public void setCompra(ComprasEntity compra) {
        this.compra = compra;
    }

    public ProductosEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductosEntity producto) {
        this.producto = producto;
    }
}
