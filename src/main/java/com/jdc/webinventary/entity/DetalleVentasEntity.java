package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "detalles_ventas")
public class DetalleVentasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddetalle_venta")
    private Integer idDetalleVenta;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Integer precioUnitario;

    @Column(name = "costo_unitario_historico")
    private Integer costoUnitarioHistorico;

    @Column(name = "medio_pago", length = 45)
    private String medioPago;

    // Relación N:1 con VentasEntity (venta_idventa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_idventa", nullable = false)
    private VentasEntity venta;

    // Relación N:1 con ProductosEntity (producto_idproducto)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_idproducto", nullable = false)
    private ProductosEntity producto;

    public DetalleVentasEntity() {
    }

    public DetalleVentasEntity(Integer idDetalleVenta, Integer cantidad, Integer precioUnitario,
                               Integer costoUnitarioHistorico, String medioPago,
                               VentasEntity venta, ProductosEntity producto) {
        this.idDetalleVenta = idDetalleVenta;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitarioHistorico = costoUnitarioHistorico;
        this.medioPago = medioPago;
        this.venta = venta;
        this.producto = producto;
    }

    public Integer getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(Integer idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Integer precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCostoUnitarioHistorico() {
        return costoUnitarioHistorico;
    }

    public void setCostoUnitarioHistorico(Integer costoUnitarioHistorico) {
        this.costoUnitarioHistorico = costoUnitarioHistorico;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public VentasEntity getVenta() {
        return venta;
    }

    public void setVenta(VentasEntity venta) {
        this.venta = venta;
    }

    public ProductosEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductosEntity producto) {
        this.producto = producto;
    }
}
