package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estados_alerta_inventario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_estado_alerta_producto", columnNames = "producto_idproducto")
})
public class EstadoAlertaInventarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idestado_alerta")
    private Integer id;

    @Column(name = "estado_alerta", nullable = false, length = 20)
    private String estado;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_idproducto", nullable = false)
    private ProductosEntity producto;

    protected EstadoAlertaInventarioEntity() { }

    public EstadoAlertaInventarioEntity(ProductosEntity producto, String estado, LocalDateTime actualizadoEn) {
        this.producto = producto;
        this.estado = estado;
        this.actualizadoEn = actualizadoEn;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    public ProductosEntity getProducto() { return producto; }
}
