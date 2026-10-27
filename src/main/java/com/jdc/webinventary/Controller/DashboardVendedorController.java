package com.jdc.webinventary.Controller;

import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.DashboardVendedorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class DashboardVendedorController {
    private final DashboardVendedorService dashboard;

    public DashboardVendedorController(DashboardVendedorService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/vendedor")
    public String mostrar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @AuthenticationPrincipal CuentaUserDetails principal,
                          Model model) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1).minusMonths(11) : desde;
        if (inicio.isAfter(fin)) {
            model.addAttribute("error", "La fecha inicial no puede ser posterior a la fecha final.");
            inicio = fin.withDayOfMonth(1).minusMonths(11);
        }
        model.addAttribute("resumen", dashboard.generar(principal.getIdUsuario(), inicio, fin));
        return "dashboard-vendedor";
    }
}
