package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroCuentaForm;
import com.jdc.webinventary.service.RegistroCuentaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registro")
public class RegistroCuentaController {

    private final RegistroCuentaService registroCuentaService;

    public RegistroCuentaController(RegistroCuentaService registroCuentaService) {
        this.registroCuentaService = registroCuentaService;
    }

    @GetMapping
    public String mostrarFormulario(@ModelAttribute("registro") RegistroCuentaForm form) {
        return "registro";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("registro") RegistroCuentaForm form,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        String correo = form.getCorreo() == null ? "" : form.getCorreo().trim();
        if (!correo.isBlank() && registroCuentaService.correoYaRegistrado(correo)) {
            bindingResult.rejectValue("correo", "correo.duplicado", "Este correo ya está registrado");
        }

        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            registroCuentaService.registrar(form);
        } catch (RegistroCuentaService.CorreoDuplicadoException ex) {
            bindingResult.rejectValue("correo", "correo.duplicado", ex.getMessage());
            return "registro";
        }

        redirectAttributes.addFlashAttribute("registroExitoso", true);
        return "redirect:/registro";
    }

    @PostMapping("/reenviar-verificacion")
    public String reenviarVerificacion(@RequestParam(required = false) String correo,
                                       RedirectAttributes redirectAttributes) {
        registroCuentaService.reenviarVerificacion(correo);
        redirectAttributes.addFlashAttribute("reenviarVerificacionSolicitado", true);
        return "redirect:/registro";
    }
}
