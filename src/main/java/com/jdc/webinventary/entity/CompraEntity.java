package com.jdc.webinventary.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class CompraEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcompra")
    private Integer idCompra;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "estado")
    private Byte estado;

    // Relación Muchos a Uno con Proveedores (proveedor_idproveedor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_idproveedor", nullable = false)
    private ProveedorEntity proveedor;

    // Relación Uno a Muchos con DetalleCompra (compra_idcompra)
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompraEntity> detalles = new ArrayList<>();

    public CompraEntity() {
    }

    public CompraEntity(Integer idCompra, LocalDate fecha, Byte estado, ProveedorEntity proveedor) {
        this.idCompra = idCompra;
        this.fecha = fecha;
        this.estado = estado;
        this.proveedor = proveedor;
    }

    public Integer getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Integer idCompra) {
        this.idCompra = idCompra;
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

    public ProveedorEntity getProveedor() {
        return proveedor;
    }

    public void setProveedor(ProveedorEntity proveedor) {
        this.proveedor = proveedor;
    }

    public List<DetalleCompraEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompraEntity> detalles) {
        this.detalles = detalles;
    }
}
