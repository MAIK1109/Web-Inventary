package com.jdc.webinventary.Controller;

import com.jdc.webinventary.service.DashboardInventarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardInventarioController {
    private final DashboardInventarioService dashboard;

    public DashboardInventarioController(DashboardInventarioService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard/inventario")
    public String mostrar(@RequestParam(required = false, defaultValue = "TODOS") String estado,
                          Model model) {
        model.addAttribute("resumen", dashboard.generar(estado));
        return "dashboard-inventario";
    }
}
