package com.jdc.webinventary.Controller;

import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.dto.RegistroAporteForm;
import com.jdc.webinventary.service.AportesService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/aportes/mi-cuenta")
public class MisAportesController {
    private final AportesService aportes;

    public MisAportesController(AportesService aportes) { this.aportes = aportes; }

    @GetMapping
    public String mostrar(@AuthenticationPrincipal CuentaUserDetails principal, Model model) {
        if (!model.containsAttribute("formAporte")) model.addAttribute("formAporte", new RegistroAporteForm());
        preparar(principal, model);
        return "aportes";
    }

    @PostMapping
    public String registrar(@AuthenticationPrincipal CuentaUserDetails principal,
                            @Valid @ModelAttribute("formAporte") RegistroAporteForm form,
                            BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preparar(principal, model);
            return "aportes";
        }
        model.addAttribute("aportes", aportes.listarUsuario(principal.getIdUsuario()));
        try {
            aportes.registrar(principal.getIdUsuario(), form.getMonto(), form.getFecha(),
                    form.getMedioPago(), form.getSoporte());
            redirect.addFlashAttribute("mensaje", "El aporte quedó pendiente de aprobación.");
        } catch (IllegalArgumentException ex) {
            binding.reject("aporte.invalido", ex.getMessage());
            preparar(principal, model);
            return "aportes";
        }
        return "redirect:/aportes/mi-cuenta";
    }

    @GetMapping("/{id}/soporte")
    public ResponseEntity<Resource> descargarSoporte(@PathVariable Integer id,
                                                     @AuthenticationPrincipal CuentaUserDetails principal) {
        try {
            Resource archivo = aportes.soporteParaUsuario(id, principal.getIdUsuario());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("soporte-aporte-" + id).build().toString()).body(archivo);
        } catch (AportesService.AporteNoEncontradoException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private void preparar(CuentaUserDetails principal, Model model) {
        model.addAttribute("aportes", aportes.listarUsuario(principal.getIdUsuario()));
        model.addAttribute("totalUsuario", aportes.totalUsuario(principal.getIdUsuario()));
    }
}
