package com.jdc.webinventary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MotivoAnulacionForm {
    @NotBlank(message = "Indica el motivo de la anulación.")
    @Size(max = 250, message = "El motivo no puede superar 250 caracteres.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
