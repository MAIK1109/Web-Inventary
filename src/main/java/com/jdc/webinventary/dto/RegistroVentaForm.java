package com.jdc.webinventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RegistroVentaForm {
    @NotNull(message = "Selecciona un cliente")
    private Integer clienteId;

    @NotBlank(message = "Selecciona el medio de pago")
    @Size(max = 45, message = "El medio de pago no puede superar 45 caracteres")
    private String medioPago;

    @NotEmpty(message = "Agrega al menos un producto")
    @Valid
    private List<DetalleVentaForm> detalles = new ArrayList<>(List.of(new DetalleVentaForm()));

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public List<DetalleVentaForm> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaForm> detalles) { this.detalles = detalles; }
}
