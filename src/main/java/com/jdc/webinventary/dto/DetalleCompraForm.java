package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DetalleCompraForm {
    @NotNull(message = "Selecciona un producto")
    private Integer productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @Min(value = 0, message = "El costo no puede ser negativo")
    private Integer costoUnitario;

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(Integer costoUnitario) { this.costoUnitario = costoUnitario; }
}
