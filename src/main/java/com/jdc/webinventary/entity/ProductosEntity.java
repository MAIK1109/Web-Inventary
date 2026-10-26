package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class ProductosEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idProducto;

    @Column(name = "codigo", length = 45)
    private String codigo;

    @Column(name = "nombre", length = 45)
    private String nombre;

    @Column(name = "precio_venta")
    private Integer precioVenta;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "estado")
    private Byte estado;

    @Column(name = "categoria_producto", length = 45)
    private String categoriaProducto;

    // Relación 1:N con DetalleComprasEntity
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleComprasEntity> detallesCompras = new ArrayList<>();

    // Relación 1:N con DetalleVentasEntity
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVentasEntity> detallesVentas = new ArrayList<>();

    // Relación 1:N con MovimientosInventariosEntity
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientosInventariosEntity> movimientosInventarios = new ArrayList<>();

    public ProductosEntity() {
    }

    public ProductosEntity(Integer idProducto, String codigo, String nombre, Integer precioVenta,
                           Integer stockActual, Integer stockMinimo, Byte estado, String categoriaProducto) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.categoriaProducto = categoriaProducto;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Integer precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Byte getEstado() {
        return estado;
    }

    public void setEstado(Byte estado) {
        this.estado = estado;
    }

    public String getCategoriaProducto() {
        return categoriaProducto;
    }

    public void setCategoriaProducto(String categoriaProducto) {
        this.categoriaProducto = categoriaProducto;
    }

    public List<DetalleComprasEntity> getDetallesCompras() {
        return detallesCompras;
    }

    public void setDetallesCompras(List<DetalleComprasEntity> detallesCompras) {
        this.detallesCompras = detallesCompras;
    }

    public List<DetalleVentasEntity> getDetallesVentas() {
        return detallesVentas;
    }

    public void setDetallesVentas(List<DetalleVentasEntity> detallesVentas) {
        this.detallesVentas = detallesVentas;
    }

    public List<MovimientosInventariosEntity> getMovimientosInventarios() {
        return movimientosInventarios;
    }

    public void setMovimientosInventarios(List<MovimientosInventariosEntity> movimientosInventarios) {
        this.movimientosInventarios = movimientosInventarios;
    }
}
