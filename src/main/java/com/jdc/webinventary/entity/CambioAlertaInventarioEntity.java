package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cambios_alerta_inventario")
public class CambioAlertaInventarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcambio_alerta")
    private Integer id;

    @Column(name = "estado_anterior", nullable = false, length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private String estadoNuevo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "existencias", nullable = false)
    private Integer existencias;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_idproducto", nullable = false)
    private ProductosEntity producto;

    protected CambioAlertaInventarioEntity() { }

    public CambioAlertaInventarioEntity(ProductosEntity producto, String estadoAnterior,
                                        String estadoNuevo, LocalDateTime fecha) {
        this.producto = producto;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
        this.existencias = producto.getStockActual() == null ? 0 : producto.getStockActual();
    }

    public Integer getId() { return id; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public LocalDateTime getFecha() { return fecha; }
    public Integer getExistencias() { return existencias; }
    public ProductosEntity getProducto() { return producto; }
}
