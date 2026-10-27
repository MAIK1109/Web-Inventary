package com.jdc.webinventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroCompraForm {
    @NotNull(message = "Selecciona un proveedor")
    private Integer proveedorId;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha = LocalDate.now();

    @NotEmpty(message = "Agrega al menos un producto a la compra")
    @Valid
    private List<DetalleCompraForm> detalles = new ArrayList<>(List.of(new DetalleCompraForm()));

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public List<DetalleCompraForm> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompraForm> detalles) { this.detalles = detalles; }
}
