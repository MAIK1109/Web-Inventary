package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.AportesService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/aportes")
public class AportesController {
    private final AportesService aportes;

    public AportesController(AportesService aportes) { this.aportes = aportes; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("aportes", aportes.listarAportes());
        model.addAttribute("totalGeneral", aportes.totalGeneral());
        model.addAttribute("totalesPorUsuario", aportes.totalesPorUsuario());
        model.addAttribute("estadoPendiente", AportesService.PENDIENTE);
        return "admin/aportes";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Integer id, Authentication authentication,
                          RedirectAttributes redirect) {
        return decidir(id, true, authentication, redirect);
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Integer id, Authentication authentication,
                           RedirectAttributes redirect) {
        return decidir(id, false, authentication, redirect);
    }

    @GetMapping("/{id}/soporte")
    public ResponseEntity<Resource> descargarSoporte(@PathVariable Integer id) {
        try {
            Resource archivo = aportes.soporteParaRevision(id);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("soporte-aporte-" + id).build().toString()).body(archivo);
        } catch (AportesService.AporteNoEncontradoException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private String decidir(Integer id, boolean aprobar, Authentication authentication,
                           RedirectAttributes redirect) {
        try {
            aportes.decidir(id, aprobar, authentication.getName());
            redirect.addFlashAttribute("mensaje", aprobar ? "Aporte aprobado." : "Aporte rechazado.");
        } catch (AportesService.AporteNoEncontradoException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/aportes";
    }
}
