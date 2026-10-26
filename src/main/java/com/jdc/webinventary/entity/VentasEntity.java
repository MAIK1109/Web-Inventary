package com.jdc.webinventary.entity;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ventas")
public class VentasEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVenta")
    private Integer idVenta;


}
