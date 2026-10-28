package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroGastoForm;
import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.DashboardFinancieroService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class DashboardFinancieroController {
    private final DashboardFinancieroService dashboard;

    public DashboardFinancieroController(DashboardFinancieroService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/financiero")
    public String mostrar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          Model model) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1).minusMonths(11) : desde;
        if (inicio.isAfter(fin)) {
            model.addAttribute("error", "La fecha inicial no puede ser posterior a la fecha final.");
            inicio = fin.withDayOfMonth(1).minusMonths(11);
        }
        cargar(model, inicio, fin);
        return "dashboard-financiero";
    }

    @PostMapping("/dashboard/financiero/gastos")
    public String registrarGasto(@Valid @ModelAttribute("gasto") RegistroGastoForm form,
                                 BindingResult binding,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                 @AuthenticationPrincipal CuentaUserDetails principal,
                                 Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            cargar(model, desde, hasta);
            return "dashboard-financiero";
        }
        try {
            dashboard.registrarGasto(form, principal.getIdUsuario());
        } catch (IllegalArgumentException ex) {
            binding.reject("gasto.invalido", ex.getMessage());
            cargar(model, desde, hasta);
            return "dashboard-financiero";
        }
        redirect.addFlashAttribute("mensaje", "El gasto quedó registrado.");
        redirect.addAttribute("desde", desde);
        redirect.addAttribute("hasta", hasta);
        return "redirect:/dashboard/financiero";
    }

    @GetMapping("/dashboard/financiero/gastos/{id}/comprobante")
    public ResponseEntity<Resource> descargarComprobante(@PathVariable Integer id) {
        try {
            Resource archivo = dashboard.comprobanteParaDescarga(id);
            String archivoOriginal = archivo.getFilename();
            String extension = archivoOriginal != null && archivoOriginal.lastIndexOf('.') >= 0
                    ? archivoOriginal.substring(archivoOriginal.lastIndexOf('.')) : "";
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("comprobante-gasto-" + id + extension).build().toString()).body(archivo);
        } catch (DashboardFinancieroService.GastoNoEncontradoException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private void cargar(Model model, LocalDate desde, LocalDate hasta) {
        model.addAttribute("resumen", dashboard.generar(desde, hasta));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        if (!model.containsAttribute("gasto")) {
            RegistroGastoForm form = new RegistroGastoForm();
            form.setFecha(LocalDate.now());
            model.addAttribute("gasto", form);
        }
    }
}
