package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class DetalleVentaForm {

    @NotNull(message = "Selecciona un producto")
    private Integer productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Integer precioUnitario;
    private Integer costoUnitarioHistorico;
    private String medioPago;

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Integer precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getCostoUnitarioHistorico() { return costoUnitarioHistorico; }
    public void setCostoUnitarioHistorico(Integer costoUnitarioHistorico) { this.costoUnitarioHistorico = costoUnitarioHistorico; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
}
