package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecuperarAccesoForm {
    @NotBlank(message = "Ingresa tu correo electrónico")
    @Email(message = "Ingresa un correo válido")
    @Size(max = 254, message = "El correo es demasiado largo")
    private String correo;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
