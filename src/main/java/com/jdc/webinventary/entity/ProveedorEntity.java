package com.jdc.webinventary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "proveedores")
public class ProveedorEntity implements Serializable {

    private static final long serialVersionUID = 1L;
}
