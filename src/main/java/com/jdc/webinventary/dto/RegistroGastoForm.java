package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class RegistroGastoForm {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor que cero")
    private Integer monto;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha = LocalDate.now();

    @NotBlank(message = "Describe el gasto")
    @Size(max = 120, message = "El concepto no puede superar 120 caracteres")
    private String concepto;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 45, message = "La categoría no puede superar 45 caracteres")
    private String categoria;

    private MultipartFile comprobante;

    public Integer getMonto() { return monto; }
    public void setMonto(Integer monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public MultipartFile getComprobante() { return comprobante; }
    public void setComprobante(MultipartFile comprobante) { this.comprobante = comprobante; }
}
