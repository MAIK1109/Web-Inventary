package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroVentaForm;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.repository.ClientesRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.RegistroVentaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
public class VentasController {
    private final RegistroVentaService registroVenta;
    private final ClientesRepository clientes;
    private final ProductosRepository productos;

    public VentasController(RegistroVentaService registroVenta, ClientesRepository clientes,
                            ProductosRepository productos) {
        this.registroVenta = registroVenta;
        this.clientes = clientes;
        this.productos = productos;
    }

    @GetMapping
    public String mostrar(Model model) {
        if (!model.containsAttribute("venta")) model.addAttribute("venta", new RegistroVentaForm());
        preparar(model);
        return "ventas";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("venta") RegistroVentaForm form,
                            BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                            Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preparar(model);
            return "ventas";
        }
        try {
            VentasEntity venta = registroVenta.registrar(form, principal.getIdUsuario());
            long total = registroVenta.calcularTotal(venta);
            redirect.addFlashAttribute("mensaje", "Venta #" + venta.getIdVenta() +
                    " confirmada. Total: " + total + ". Se registraron los movimientos de salida.");
        } catch (RegistroVentaService.StockInsuficienteException | IllegalArgumentException ex) {
            binding.reject("venta.invalida", ex.getMessage());
            preparar(model);
            return "ventas";
        }
        return "redirect:/ventas";
    }

    private void preparar(Model model) {
        model.addAttribute("clientes", clientes.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("productos", productos.findByEstadoIsNullOrEstado((byte) 1, Sort.by("nombre").ascending()));
        var ventasRegistradas = registroVenta.listar();
        Map<Integer, Long> totales = new LinkedHashMap<>();
        for (VentasEntity venta : ventasRegistradas) {
            totales.put(venta.getIdVenta(), registroVenta.calcularTotal(venta));
        }
        model.addAttribute("ventas", ventasRegistradas);
        model.addAttribute("totales", totales);
    }
}
