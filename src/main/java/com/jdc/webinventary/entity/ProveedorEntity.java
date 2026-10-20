package com.jdc.webinventary.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "proveedores")
public class ProveedorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproveedor")
    private Integer idProveedor;
}
