package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class ClientesEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Integer idCliente;

    @Column(name = "identificacion", length = 45)
    private String identificacion;

    @Column(name = "nombre", length = 45)
    private String nombre;

    @Column(name = "telefono", length = 45)
    private String telefono;

    @Column(name = "correo", length = 45)
    private String correo;

    @Column(name = "direccion", length = 45)
    private String direccion;

    // Relación Uno a Muchos con Ventas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentasEntity> ventas = new ArrayList<>();

    public ClientesEntity() {
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<VentasEntity> getVentas() {
        return ventas;
    }

    public void setVentas(List<VentasEntity> ventas) {
        this.ventas = ventas;
    }
}