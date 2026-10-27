package com.jdc.webinventary.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class ComprasEntity implements Serializable {

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
    private ProveedoresEntity proveedor;

    // Relación Uno a Muchos con DetalleCompra (compra_idcompra)
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleComprasEntity> detalles = new ArrayList<>();

    public ComprasEntity() {
    }

    public ComprasEntity(Integer idCompra, LocalDate fecha, Byte estado, ProveedoresEntity proveedor) {
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

    public ProveedoresEntity getProveedor() {
        return proveedor;
    }

    public void setProveedor(ProveedoresEntity proveedor) {
        this.proveedor = proveedor;
    }

    public List<DetalleComprasEntity> getDetalles() {
        return detalles;
    }

    public void addDetalle(DetalleComprasEntity detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }

    public void setDetalles(List<DetalleComprasEntity> detalles) {
        this.detalles = detalles;
    }
}
