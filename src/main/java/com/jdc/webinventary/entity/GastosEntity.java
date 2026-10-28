package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "gastos")
public class GastosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idgasto")
    private Integer idGasto;

    @Column(name = "monto", nullable = false)
    private Integer monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "concepto", nullable = false, length = 120)
    private String concepto;

    @Column(name = "categoria", length = 45)
    private String categoria;

    @Column(name = "comprobante_archivo", length = 100)
    private String comprobanteArchivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_idusuario", nullable = false)
    private UsuariosEntity usuario;

    protected GastosEntity() { }

    public GastosEntity(Integer monto, LocalDate fecha, String concepto, String categoria, UsuariosEntity usuario) {
        this.monto = monto;
        this.fecha = fecha;
        this.concepto = concepto;
        this.categoria = categoria;
        this.usuario = usuario;
    }

    public Integer getIdGasto() { return idGasto; }
    public Integer getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getConcepto() { return concepto; }
    public String getCategoria() { return categoria; }
    public String getComprobanteArchivo() { return comprobanteArchivo; }
    public void setComprobanteArchivo(String comprobanteArchivo) { this.comprobanteArchivo = comprobanteArchivo; }
    public UsuariosEntity getUsuario() { return usuario; }
}
