package com.jdc.webinventary.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ventas")
public class VentaEntity implements Serializable {
    private static final long serialVersionUID = 1L;
}
