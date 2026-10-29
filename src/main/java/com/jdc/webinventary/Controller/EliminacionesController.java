package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.MotivoAnulacionForm;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.EliminacionRestringidaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class EliminacionesController {
    private final EliminacionRestringidaService eliminaciones;

    public EliminacionesController(EliminacionRestringidaService eliminaciones) { this.eliminaciones = eliminaciones; }

    @GetMapping("/admin/anulaciones")
    public String historial(Model model) {
        model.addAttribute("anulaciones", eliminaciones.historial());
        var ventasConfirmadas = eliminaciones.ventasConfirmadasParaAnular();
        Map<Integer, Long> totalesVentas = new HashMap<>();
        for (VentasEntity venta : ventasConfirmadas) {
            long total = venta.getDetalles().stream().mapToLong(detalle ->
                    (long) (detalle.getCantidad() == null ? 0 : detalle.getCantidad())
                            * (detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario())).sum();
            totalesVentas.put(venta.getIdVenta(), total);
        }
        model.addAttribute("ventasConfirmadas", ventasConfirmadas);
        model.addAttribute("totalesVentas", totalesVentas);
        return "admin/anulaciones";
    }

    @PostMapping("/admin/anulaciones/venta/{id}")
    public String anularVenta(@PathVariable Integer id,
                         @Valid @ModelAttribute("form") MotivoAnulacionForm form,
                         BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("error", binding.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/anulaciones";
        }
        try {
            eliminaciones.anularVenta(id, form.getMotivo(), principal.getIdUsuario());
            redirect.addFlashAttribute("mensaje", "Venta " + id + " anulada con trazabilidad.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/anulaciones";
    }

    @PostMapping("/admin/eliminaciones/{tipo}/{id}")
    public String eliminar(@PathVariable String tipo, @PathVariable Integer id,
                           @AuthenticationPrincipal CuentaUserDetails principal, RedirectAttributes redirect) {
        String destino = switch (tipo.toUpperCase()) {
            case "PRODUCTO" -> "/productos";
            case "CLIENTE" -> "/clientes";
            case "PROVEEDOR" -> "/proveedores";
            default -> "/";
        };
        try {
            eliminaciones.eliminarMaestro(tipo, id, principal.getIdUsuario());
            redirect.addFlashAttribute("mensaje", "Registro eliminado y acción auditada.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "El registro tiene dependencias y no se puede eliminar.");
        }
        return "redirect:" + destino;
    }
}
