package com.jdc.webinventary.Controller;

import com.jdc.webinventary.security.CuentaUserDetails;
import com.jdc.webinventary.service.ReportesExportacionService;
import com.jdc.webinventary.service.ReportesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
public class ReportesController {
    private static final String EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ReportesService reportes;
    private final ReportesExportacionService exportador;

    public ReportesController(ReportesService reportes, ReportesExportacionService exportador) {
        this.reportes = reportes; this.exportador = exportador;
    }

    @GetMapping("/reportes")
    public String mostrar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) Integer productoId,
                          @RequestParam(required = false) Integer clienteId,
                          @RequestParam(required = false) Integer vendedorId,
                          @AuthenticationPrincipal CuentaUserDetails principal, Model model) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1).minusMonths(11) : desde;
        boolean puedeVerTodos = puedeVerTodos(principal);
        if (inicio.isAfter(fin)) {
            model.addAttribute("error", "La fecha inicial no puede ser posterior a la fecha final.");
            inicio = fin.withDayOfMonth(1).minusMonths(11);
        }
        ReportesService.Resultado resultado;
        try {
            resultado = reportes.filtrar(inicio, fin, productoId, clienteId, vendedorId,
                    principal.getIdUsuario(), puedeVerTodos);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            resultado = new ReportesService.Resultado(inicio, fin, java.util.List.of(), 0, 0);
        }
        cargarFiltros(model, resultado, productoId, clienteId, puedeVerTodos ? vendedorId : null, puedeVerTodos);
        return "reportes";
    }

    @GetMapping("/reportes/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) Integer productoId,
                          @RequestParam(required = false) Integer clienteId,
                          @RequestParam(required = false) Integer vendedorId,
                          @AuthenticationPrincipal CuentaUserDetails principal) {
        ReportesService.Resultado resultado;
        try {
            resultado = reportes.filtrar(desde, hasta, productoId, clienteId, vendedorId,
                    principal.getIdUsuario(), puedeVerTodos(principal));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage().getBytes(StandardCharsets.UTF_8));
        }
        String tipo = formato.toLowerCase();
        byte[] contenido;
        String extension;
        MediaType media;
        if ("pdf".equals(tipo)) {
            contenido = exportador.pdf(resultado); extension = "pdf"; media = MediaType.APPLICATION_PDF;
        } else if ("excel".equals(tipo) || "xlsx".equals(tipo)) {
            contenido = exportador.excel(resultado); extension = "xlsx"; media = MediaType.parseMediaType(EXCEL);
        } else {
            return ResponseEntity.notFound().build();
        }
        String nombre = "reporte-ventas-" + desde + "-" + hasta + "." + extension;
        return ResponseEntity.ok().contentType(media).header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(nombre).build().toString()).body(contenido);
    }

    private boolean puedeVerTodos(CuentaUserDetails principal) {
        return principal.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPERADMIN".equals(a.getAuthority()));
    }

    private void cargarFiltros(Model model, ReportesService.Resultado resultado, Integer productoId,
                               Integer clienteId, Integer vendedorId, boolean puedeVerTodos) {
        model.addAttribute("reporte", resultado);
        model.addAttribute("desde", resultado.desde()); model.addAttribute("hasta", resultado.hasta());
        model.addAttribute("productoId", productoId); model.addAttribute("clienteId", clienteId);
        model.addAttribute("vendedorId", vendedorId); model.addAttribute("puedeVerTodos", puedeVerTodos);
        model.addAttribute("productos", reportes.listarProductos());
        model.addAttribute("clientes", reportes.listarClientes());
        if (puedeVerTodos) model.addAttribute("vendedores", reportes.listarVendedores());
    }
}
