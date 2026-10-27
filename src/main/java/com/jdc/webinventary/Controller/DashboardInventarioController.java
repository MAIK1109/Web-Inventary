package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.DashboardInventarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardInventarioController {
    private final DashboardInventarioService dashboard;

    public DashboardInventarioController(DashboardInventarioService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/inventario")
    public String mostrar(Model model) {
        model.addAttribute("resumen", dashboard.generar());
        return "dashboard-inventario";
    }
}
