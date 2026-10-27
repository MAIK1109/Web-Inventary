package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.VerificacionCorreoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VerificacionCorreoController {
    private final VerificacionCorreoService verificacion;

    public VerificacionCorreoController(VerificacionCorreoService verificacion) {
        this.verificacion = verificacion;
    }

    @GetMapping("/verificar-correo")
    public String verificar(@RequestParam(required = false) String token, RedirectAttributes redirect) {
        if (verificacion.verificar(token)) {
            redirect.addFlashAttribute("correoVerificado", true);
        } else {
            redirect.addFlashAttribute("verificacionInvalida", true);
        }
        return "redirect:/login";
    }
}
