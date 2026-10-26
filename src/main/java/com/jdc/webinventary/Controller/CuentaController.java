package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.CuentaPerfilForm;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.AdministracionUsuariosService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cuenta")
public class CuentaController {

    private final AdministracionUsuariosService administracion;

    public CuentaController(AdministracionUsuariosService administracion) {
        this.administracion = administracion;
    }

    @GetMapping
    public String mostrar(@AuthenticationPrincipal CuentaUserDetails principal, Model model) {
        UsuariosEntity usuario = administracion.obtenerUsuario(principal.getIdUsuario());
        model.addAttribute("cuenta", convertir(usuario));
        return "cuenta/perfil";
    }

    @PostMapping
    public String actualizar(@AuthenticationPrincipal CuentaUserDetails principal,
                             @Valid @ModelAttribute("cuenta") CuentaPerfilForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        String correoOriginal = principal.getUsername();
        if (!bindingResult.hasErrors()) {
            try {
                administracion.actualizarPerfil(principal.getIdUsuario(), form);
            } catch (AdministracionUsuariosService.CorreoDuplicadoException ex) {
                bindingResult.rejectValue("correo", "correo.duplicado", ex.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            return "cuenta/perfil";
        }

        if (!correoOriginal.equalsIgnoreCase(form.getCorreo().trim())) {
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?correoActualizado";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Tus datos fueron actualizados.");
        return "redirect:/cuenta";
    }

    private CuentaPerfilForm convertir(UsuariosEntity usuario) {
        CuentaPerfilForm form = new CuentaPerfilForm();
        form.setIdentificacion(usuario.getIdentificacion());
        form.setNombre(usuario.getNombre());
        form.setTelefono(usuario.getTelefono());
        form.setCorreo(usuario.getCorreo());
        return form;
    }
}
