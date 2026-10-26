package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CuentaPerfilForm {
    @NotBlank(message = "La identificación es obligatoria")
    @Size(max = 45, message = "La identificación no puede superar 45 caracteres")
    private String identificacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 45, message = "El nombre no puede superar 45 caracteres")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 45, message = "El teléfono no puede superar 45 caracteres")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres")
    private String correo;

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
