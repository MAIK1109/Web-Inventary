package com.jdc.webinventary.Controller;

import com.jdc.webinventary.repository.UsuariosRepository;
import com.jdc.webinventary.service.BusquedaService;
import com.jdc.webinventary.security.CuentaUserDetails;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class BusquedaController {
    private final BusquedaService busqueda;
    private final UsuariosRepository usuarios;

    public BusquedaController(BusquedaService busqueda, UsuariosRepository usuarios) {
        this.busqueda = busqueda;
        this.usuarios = usuarios;
    }

    @GetMapping("/busqueda")
    public String buscar(@RequestParam(required = false) String producto,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(required = false) Integer usuarioId,
                         @RequestParam(required = false) Byte estado,
                         @AuthenticationPrincipal CuentaUserDetails principal, Model model) {
        boolean superadmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
        Integer usuarioVisible = superadmin ? usuarioId : principal.getIdUsuario();
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            model.addAttribute("error", "La fecha inicial no puede ser posterior a la fecha final.");
        } else {
            model.addAttribute("transacciones", busqueda.buscarTransacciones(desde, hasta, usuarioVisible, estado));
        }
        model.addAttribute("productos", busqueda.buscarProductos(producto));
        if (superadmin) model.addAttribute("usuarios", usuarios.findAll(Sort.by("nombre").ascending()));
        model.addAttribute("filtroProducto", producto);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("usuarioId", usuarioVisible);
        model.addAttribute("estado", estado);
        return "busqueda";
    }
}
