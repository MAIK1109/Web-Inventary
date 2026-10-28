package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroClienteForm;
import com.jdc.webinventary.service.RegistroClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClientesController {

    private final RegistroClienteService registroClienteService;

    public ClientesController(RegistroClienteService registroClienteService) {
        this.registroClienteService = registroClienteService;
    }

    @GetMapping
    public String mostrar(Model model) {
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", new RegistroClienteForm());
        }
        model.addAttribute("clientes", registroClienteService.listar());
        return "clientes";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("cliente") RegistroClienteForm form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (form.isConsentimientoComprobantes()
                && (form.getCorreo() == null || form.getCorreo().isBlank())) {
            bindingResult.rejectValue("correo", "correo.requerido", "El correo es necesario para enviar los comprobantes");
        }
        if (!bindingResult.hasErrors()
                && registroClienteService.identificacionRegistrada(form.getIdentificacion())) {
            bindingResult.rejectValue("identificacion", "identificacion.duplicada", "Esta identificación ya está registrada");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("clientes", registroClienteService.listar());
            return "clientes";
        }

        try {
            registroClienteService.registrar(form);
        } catch (RegistroClienteService.IdentificacionDuplicadaException ex) {
            bindingResult.rejectValue("identificacion", "identificacion.duplicada", ex.getMessage());
            model.addAttribute("clientes", registroClienteService.listar());
            return "clientes";
        }

        redirectAttributes.addFlashAttribute("mensaje", "El cliente fue registrado correctamente.");
        return "redirect:/clientes";
    }
}
