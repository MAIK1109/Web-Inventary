package com.jdc.webinventary.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "compras")
public class ComprasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcompra")
    private Integer idCompra;
}
