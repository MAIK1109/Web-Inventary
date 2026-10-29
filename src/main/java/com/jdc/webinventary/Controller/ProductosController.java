package com.jdc.webinventary.Controller;

import com.jdc.webinventary.dto.ProductoForm;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.service.ProductosService;
import com.jdc.webinventary.security.CuentaUserDetails;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductosController {
    private final ProductosService productos;

    public ProductosController(ProductosService productos) {
        this.productos = productos;
    }

    @GetMapping
    public String listar(Model model) {
        if (!model.containsAttribute("producto")) model.addAttribute("producto", new ProductoForm());
        prepararVista(model, null);
        return "productos";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("producto") ProductoForm form,
                        BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                        Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors() && productos.codigoRegistrado(form.getCodigo(), null)) {
            binding.rejectValue("codigo", "codigo.duplicado", "Ya existe un producto con ese código.");
        }
        if (binding.hasErrors()) {
            prepararVista(model, null);
            return "productos";
        }
        try {
            productos.crear(form, principal.getIdUsuario());
        } catch (ProductosService.CodigoDuplicadoException ex) {
            binding.rejectValue("codigo", "codigo.duplicado", ex.getMessage());
            prepararVista(model, null);
            return "productos";
        }
        redirect.addFlashAttribute("mensaje", "El producto fue creado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        ProductosEntity entidad = productos.obtener(id);
        if (!model.containsAttribute("producto")) model.addAttribute("producto", convertir(entidad));
        prepararVista(model, id);
        return "productos";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("producto") ProductoForm form,
                             BindingResult binding, @AuthenticationPrincipal CuentaUserDetails principal,
                             Model model, RedirectAttributes redirect) {
        productos.obtener(id);
        if (!binding.hasErrors() && productos.codigoRegistrado(form.getCodigo(), id)) {
            binding.rejectValue("codigo", "codigo.duplicado", "Ya existe otro producto con ese código.");
        }
        if (binding.hasErrors()) {
            prepararVista(model, id);
            return "productos";
        }
        try {
            productos.actualizar(id, form, principal.getIdUsuario());
        } catch (ProductosService.CodigoDuplicadoException ex) {
            binding.rejectValue("codigo", "codigo.duplicado", ex.getMessage());
            prepararVista(model, id);
            return "productos";
        }
        redirect.addFlashAttribute("mensaje", "El producto fue actualizado correctamente.");
        return "redirect:/productos";
    }

    private void prepararVista(Model model, Integer id) {
        model.addAttribute("productos", productos.listar());
        model.addAttribute("productoId", id);
        model.addAttribute("formAction", id == null ? "/productos" : "/productos/" + id);
    }

    private ProductoForm convertir(ProductosEntity entidad) {
        ProductoForm form = new ProductoForm();
        form.setCodigo(entidad.getCodigo());
        form.setNombre(entidad.getNombre());
        form.setCategoriaProducto(entidad.getCategoriaProducto());
        form.setDescripcion(entidad.getDescripcion());
        form.setPrecioCompraReferencia(entidad.getPrecioCompraReferencia());
        form.setPrecioVenta(entidad.getPrecioVenta());
        form.setStockMinimo(entidad.getStockMinimo());
        form.setEstado(entidad.getEstado() == null ? (byte) 1 : entidad.getEstado());
        return form;
    }
}
