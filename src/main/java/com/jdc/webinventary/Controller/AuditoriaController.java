package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.AuditoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditoriaController {
    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) { this.auditoria = auditoria; }

    @GetMapping("/admin/auditoria")
    public String historial(Model model) {
        model.addAttribute("eventos", auditoria.recientes());
        return "admin/auditoria";
    }
}
