package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.RegistroCompraForm;
import com.jdc.webinventary.repository.ProductosRepository;
import com.jdc.webinventary.repository.ProveedoresRepository;
import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.RegistroCompraService;
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

@Controller
@RequestMapping("/compras")
public class ComprasController {
    private final RegistroCompraService registroCompra;
    private final ProveedoresRepository proveedores;
    private final ProductosRepository productos;

    public ComprasController(RegistroCompraService registroCompra, ProveedoresRepository proveedores,
                             ProductosRepository productos) {
        this.registroCompra = registroCompra;
        this.proveedores = proveedores;
        this.productos = productos;
    }

    @GetMapping
    public String mostrar(Model model) {
        if (!model.containsAttribute("compra")) model.addAttribute("compra", new RegistroCompraForm());
        preparar(model);
        return "compras";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("compra") RegistroCompraForm form,
                            BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                            Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preparar(model);
            return "compras";
        }
        try {
            registroCompra.registrarYConfirmar(form, principal.getIdUsuario());
        } catch (IllegalArgumentException ex) {
            binding.reject("compra.invalida", ex.getMessage());
            preparar(model);
            return "compras";
        }
        redirect.addFlashAttribute("mensaje", "Compra confirmada. Se actualizaron existencias y movimientos de entrada.");
        return "redirect:/compras";
    }

    private void preparar(Model model) {
        model.addAttribute("proveedores", proveedores.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("productos", productos.findByEstadoIsNullOrEstado((byte) 1, Sort.by("nombre").ascending()));
        model.addAttribute("compras", registroCompra.listar());
    }
}
