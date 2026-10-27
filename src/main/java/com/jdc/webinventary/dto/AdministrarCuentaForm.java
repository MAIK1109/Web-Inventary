package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public class AdministrarCuentaForm extends CuentaPerfilForm {
    @NotBlank(message = "Selecciona un rol")
    @Pattern(regexp = "USUARIO|ADMINISTRADOR|SUPERADMIN", message = "El rol seleccionado no es válido")
    private String rol;

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
