package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class RegistroAporteForm {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor que cero")
    private Integer monto;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha = LocalDate.now();

    @NotBlank(message = "Selecciona el medio de pago")
    @Size(max = 45, message = "El medio de pago no puede superar 45 caracteres")
    private String medioPago;

    private MultipartFile soporte;

    public Integer getMonto() { return monto; }
    public void setMonto(Integer monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public MultipartFile getSoporte() { return soporte; }
    public void setSoporte(MultipartFile soporte) { this.soporte = soporte; }
}
