package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_proveedores_identificacion", columnNames = "identificacion")
})
public class ProveedoresEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproveedor")
    private Integer idProveedor;

    @Column(name = "identificacion", length = 45)
    private String identificacion;

    @Column(name = "nombre", length = 45)
    private String nombre;

    @Column(name = "telefono", length = 45)
    private String telefono;

    @Column(name = "correo", length = 254)
    private String correo;

    @Column(name = "direccion", length = 45)
    private String direccion;

    @Column(name = "nombre_contacto", length = 45)
    private String nombreContacto;

    // Relación 1:N con ComprasEntity
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComprasEntity> compras = new ArrayList<>();

    public ProveedoresEntity() {
    }

    public ProveedoresEntity(Integer idProveedor, String identificacion, String nombre,
                             String telefono, String correo, String direccion, String nombreContacto) {
        this.idProveedor = idProveedor;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.nombreContacto = nombreContacto;
    }

    // Métodos de conveniencia para la relación bidireccional con ComprasEntity
    public void addCompra(ComprasEntity compra) {
        compras.add(compra);
        compra.setProveedor(this);
    }

    public void removeCompra(ComprasEntity compra) {
        compras.remove(compra);
        compra.setProveedor(null);
    }

    // Getters y Setters
    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
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

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public List<ComprasEntity> getCompras() {
        return compras;
    }

    public void setCompras(List<ComprasEntity> compras) {
        this.compras = compras;
    }
}
