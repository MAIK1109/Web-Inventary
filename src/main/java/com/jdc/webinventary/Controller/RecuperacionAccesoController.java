package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.NuevaPasswordForm;
import com.jdc.webinventary.dto.RecuperarAccesoForm;
import com.jdc.webinventary.service.RecuperacionAccesoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar-acceso")
public class RecuperacionAccesoController {
    private final RecuperacionAccesoService recuperacion;

    public RecuperacionAccesoController(RecuperacionAccesoService recuperacion) {
        this.recuperacion = recuperacion;
    }

    @GetMapping
    public String mostrarSolicitud(Model model) {
        if (!model.containsAttribute("recuperacion")) model.addAttribute("recuperacion", new RecuperarAccesoForm());
        return "recuperar-acceso";
    }

    @PostMapping("/solicitar")
    public String solicitar(@Valid @ModelAttribute("recuperacion") RecuperarAccesoForm form,
                            BindingResult binding, Model model) {
        if (binding.hasErrors()) return "recuperar-acceso";
        recuperacion.solicitar(form.getCorreo());
        model.addAttribute("solicitado", true);
        return "recuperar-acceso";
    }

    @GetMapping("/nueva")
    public String mostrarNueva(@RequestParam(required = false) String token, Model model) {
        boolean vigente = recuperacion.tokenVigente(token);
        model.addAttribute("tokenVigente", vigente);
        NuevaPasswordForm form = new NuevaPasswordForm();
        form.setToken(token);
        model.addAttribute("nuevaPassword", form);
        return "recuperar-nueva";
    }

    @PostMapping("/nueva")
    public String cambiar(@Valid @ModelAttribute("nuevaPassword") NuevaPasswordForm form,
                          BindingResult binding, Model model, RedirectAttributes redirect) {
        if (form.getPassword() != null && form.getConfirmarPassword() != null
                && !form.getPassword().equals(form.getConfirmarPassword())) {
            binding.rejectValue("confirmarPassword", "password.noCoincide", "Las contraseñas no coinciden.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("tokenVigente", recuperacion.tokenVigente(form.getToken()));
            return "recuperar-nueva";
        }
        if (!recuperacion.cambiarPassword(form.getToken(), form.getPassword())) {
            model.addAttribute("tokenVigente", false);
            model.addAttribute("errorToken", "El enlace venció o ya fue utilizado. Solicita uno nuevo.");
            return "recuperar-nueva";
        }
        redirect.addFlashAttribute("passwordActualizada", true);
        return "redirect:/login";
    }
}
