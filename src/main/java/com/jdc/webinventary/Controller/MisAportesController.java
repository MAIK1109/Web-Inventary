package com.jdc.webinventary.Controller;

import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.AportesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/aportes/mi-cuenta")
public class MisAportesController {
    private final AportesService aportes;

    public MisAportesController(AportesService aportes) { this.aportes = aportes; }

    @GetMapping
    public String mostrar(@AuthenticationPrincipal CuentaUserDetails principal, Model model) {
        model.addAttribute("aportes", aportes.listarUsuario(principal.getIdUsuario()));
        model.addAttribute("totalUsuario", aportes.totalUsuario(principal.getIdUsuario()));
        return "aportes";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal CuentaUserDetails principal,
                            @RequestParam int monto, @RequestParam String medioPago,
                            RedirectAttributes redirect) {
        try {
            aportes.registrar(principal.getIdUsuario(), monto, medioPago);
            redirect.addFlashAttribute("mensaje", "El aporte quedó pendiente de aprobación.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/aportes/mi-cuenta";
    }
}
