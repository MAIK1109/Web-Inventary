package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroProveedorForm;
import com.jdc.webinventary.service.RegistroProveedorService;
import com.jdc.webinventary.security.CuentaUserDetails;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedores")
public class ProveedoresController {
    private final RegistroProveedorService registroProveedor;

    public ProveedoresController(RegistroProveedorService registroProveedor) {
        this.registroProveedor = registroProveedor;
    }

    @GetMapping
    public String mostrar(Model model) {
        if (!model.containsAttribute("proveedor")) model.addAttribute("proveedor", new RegistroProveedorForm());
        model.addAttribute("proveedores", registroProveedor.listar());
        return "proveedores";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("proveedor") RegistroProveedorForm form,
                            BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                            Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors() && registroProveedor.identificacionRegistrada(form.getIdentificacion())) {
            binding.rejectValue("identificacion", "identificacion.duplicada",
                    "Ya existe un proveedor con esa identificación.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("proveedores", registroProveedor.listar());
            return "proveedores";
        }
        try {
            registroProveedor.registrar(form, principal.getIdUsuario());
        } catch (RegistroProveedorService.IdentificacionDuplicadaException ex) {
            binding.rejectValue("identificacion", "identificacion.duplicada", ex.getMessage());
            model.addAttribute("proveedores", registroProveedor.listar());
            return "proveedores";
        }
        redirect.addFlashAttribute("mensaje", "El proveedor fue registrado correctamente.");
        return "redirect:/proveedores";
    }
}
