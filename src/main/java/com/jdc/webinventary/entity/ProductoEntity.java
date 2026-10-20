package com.jdc.webinventary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "productos")
public class ProductoEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
