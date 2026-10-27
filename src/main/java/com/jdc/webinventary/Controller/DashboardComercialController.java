package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.DashboardComercialService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class DashboardComercialController {
    private final DashboardComercialService dashboard;

    public DashboardComercialController(DashboardComercialService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/comercial")
    public String mostrar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          Model model) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1) : desde;
        model.addAttribute("resumen", dashboard.generar(inicio, fin));
        return "dashboard-comercial";
    }
}
