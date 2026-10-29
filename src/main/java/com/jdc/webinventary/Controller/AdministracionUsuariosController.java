package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.AdministrarCuentaForm;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.service.AdministracionUsuariosService;
import com.jdc.webinventary.security.CuentaUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdministracionUsuariosController {

    private final AdministracionUsuariosService administracion;

    public AdministracionUsuariosController(AdministracionUsuariosService administracion) {
        this.administracion = administracion;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", administracion.listarUsuarios());
        return "admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        UsuariosEntity usuario = administracion.obtenerUsuario(id);
        AdministrarCuentaForm form = new AdministrarCuentaForm();
        form.setIdentificacion(usuario.getIdentificacion());
        form.setNombre(usuario.getNombre());
        form.setTelefono(usuario.getTelefono());
        form.setCorreo(usuario.getCorreo());
        form.setRol(usuario.getRol() == null ? "USUARIO" : usuario.getRol());
        model.addAttribute("usuarioId", id);
        model.addAttribute("cuenta", form);
        model.addAttribute("usuario", usuario);
        return "admin/usuario-form";
    }

    @PostMapping("/{id}")
    public String guardar(@PathVariable Integer id,
                          @Valid @ModelAttribute("cuenta") AdministrarCuentaForm form,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal CuentaUserDetails principal,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("usuario", administracion.obtenerUsuario(id));
            return "admin/usuario-form";
        }
        try {
            administracion.actualizarComoSuperadmin(id, form, principal.getIdUsuario());
        } catch (AdministracionUsuariosService.CorreoDuplicadoException ex) {
            bindingResult.rejectValue("correo", "correo.duplicado", ex.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("usuario", administracion.obtenerUsuario(id));
            return "admin/usuario-form";
        } catch (AdministracionUsuariosService.OperacionNoPermitidaException ex) {
            bindingResult.rejectValue("rol", "rol.noPermitido", ex.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("usuario", administracion.obtenerUsuario(id));
            return "admin/usuario-form";
        }
        redirectAttributes.addFlashAttribute("mensaje", "Los datos de la cuenta fueron actualizados.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Integer id, @AuthenticationPrincipal CuentaUserDetails principal,
                          RedirectAttributes redirectAttributes) {
        try {
            administracion.activar(id, principal.getIdUsuario());
            redirectAttributes.addFlashAttribute("mensaje", "La cuenta quedó activa; el usuario también debe verificar su correo para iniciar sesión.");
        } catch (AdministracionUsuariosService.OperacionNoPermitidaException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/bloquear")
    public String bloquear(@PathVariable Integer id, Authentication authentication,
                           @AuthenticationPrincipal CuentaUserDetails principal,
                           RedirectAttributes redirectAttributes) {
        try {
            administracion.bloquear(id, authentication.getName(), principal.getIdUsuario());
            redirectAttributes.addFlashAttribute("mensaje", "La cuenta quedó bloqueada y sus sesiones fueron cerradas.");
        } catch (AdministracionUsuariosService.OperacionNoPermitidaException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
