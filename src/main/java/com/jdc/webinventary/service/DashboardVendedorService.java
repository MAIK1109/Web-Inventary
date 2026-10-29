package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.InversionesRepository;
import com.jdc.webinventary.repository.VentasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardVendedorService {
    private final VentasRepository ventasRepository;
    private final InversionesRepository inversionesRepository;

    public DashboardVendedorService(VentasRepository ventasRepository, InversionesRepository inversionesRepository) {
        this.ventasRepository = ventasRepository;
        this.inversionesRepository = inversionesRepository;
    }

    @Transactional(readOnly = true)
    public Resumen generar(Integer usuarioId, LocalDate desde, LocalDate hasta) {
        if (usuarioId == null || desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El período seleccionado no es válido.");
        }
        List<VentasEntity> ventasPropias = ventasRepository
                .findByVendedor_IdUsuarioAndFechaBetweenOrderByFechaDesc(usuarioId, desde, hasta);
        List<InversionesEntity> aportesPropios = inversionesRepository
                .findByUsuario_IdUsuarioAndFechaBetweenOrderByFechaDesc(usuarioId, desde, hasta);

        Map<YearMonth, long[]> meses = new TreeMap<>();
        for (YearMonth mes = YearMonth.from(desde); !mes.isAfter(YearMonth.from(hasta)); mes = mes.plusMonths(1)) {
            meses.put(mes, new long[2]); // ventas y aportes confirmados
        }

        long totalVentas = 0;
        long aportesConfirmados = 0;
        long aportesPendientes = 0;
        Map<String, Long> cantidadesProducto = new HashMap<>();
        List<MovimientoPersonal> historial = new ArrayList<>();
        List<VentaPersonal> detalleVentas = new ArrayList<>();

        for (VentasEntity venta : ventasPropias) {
            long totalVenta = totalVenta(venta);
            boolean confirmada = venta.getEstado() == null || venta.getEstado() == 1;
            if (confirmada) {
                totalVentas = Math.addExact(totalVentas, totalVenta);
                meses.get(YearMonth.from(venta.getFecha()))[0] = Math.addExact(
                        meses.get(YearMonth.from(venta.getFecha()))[0], totalVenta);
                for (DetalleVentasEntity detalle : venta.getDetalles()) {
                    if (detalle.getProducto() != null && detalle.getProducto().getNombre() != null) {
                        cantidadesProducto.merge(detalle.getProducto().getNombre(),
                                (long) (detalle.getCantidad() == null ? 0 : detalle.getCantidad()), Long::sum);
                    }
                }
            }
            historial.add(new MovimientoPersonal("Venta #" + venta.getIdVenta(), venta.getFecha(), totalVenta,
                    confirmada ? "CONFIRMADA" : textoEstado(venta.getEstado())));
            String cliente = venta.getCliente() == null ? "Sin cliente" : venta.getCliente().getNombre();
            detalleVentas.add(new VentaPersonal(venta.getIdVenta(), venta.getFecha(), cliente, totalVenta,
                    venta.getDetalles().size(), confirmada ? "CONFIRMADA" : textoEstado(venta.getEstado())));
        }

        for (InversionesEntity aporte : aportesPropios) {
            boolean confirmada = aporte.getEstado() != null && aporte.getEstado() == 1;
            if (confirmada) {
                long monto = aporte.getMonto() == null ? 0 : aporte.getMonto();
                aportesConfirmados = Math.addExact(aportesConfirmados, monto);
                long[] filaMes = meses.get(YearMonth.from(aporte.getFecha()));
                filaMes[1] = Math.addExact(filaMes[1], monto);
            } else if (aporte.getEstado() != null && aporte.getEstado() == 0) {
                aportesPendientes = Math.addExact(aportesPendientes,
                        aporte.getMonto() == null ? 0 : aporte.getMonto());
            }
            historial.add(new MovimientoPersonal("Aporte", aporte.getFecha(),
                    aporte.getMonto() == null ? 0 : aporte.getMonto(), textoEstado(aporte.getEstado())));
        }

        historial.sort(Comparator.comparing(MovimientoPersonal::fecha,
                Comparator.nullsLast(Comparator.reverseOrder())));
        List<ProductoVendido> productos = cantidadesProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new ProductoVendido(entry.getKey(), entry.getValue())).toList();
        List<EvolucionMensual> evolucion = meses.entrySet().stream()
                .map(entry -> new EvolucionMensual(entry.getKey().toString(), entry.getValue()[0], entry.getValue()[1]))
                .toList();
        return new Resumen(desde, hasta, aportesConfirmados, aportesPendientes, totalVentas,
                (int) ventasPropias.stream().filter(v -> v.getEstado() == null || v.getEstado() == 1).count(),
                (int) aportesPropios.stream().filter(a -> a.getEstado() != null && a.getEstado() == 1).count(),
                (int) aportesPropios.stream().filter(a -> a.getEstado() != null && a.getEstado() == 0).count(),
                productos, evolucion, detalleVentas, historial);
    }

    private long totalVenta(VentasEntity venta) {
        long total = 0;
        for (DetalleVentasEntity detalle : venta.getDetalles()) {
            long cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
            long precio = detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario();
            total = Math.addExact(total, Math.multiplyExact(cantidad, precio));
        }
        return total;
    }

    private String textoEstado(Byte estado) {
        if (estado == null) return "SIN ESTADO";
        return switch (estado) {
            case 0 -> "PENDIENTE";
            case 1 -> "CONFIRMADO";
            case 2 -> "RECHAZADO";
            case 3 -> "ANULADO";
            default -> estado.toString();
        };
    }

    public record ProductoVendido(String nombre, long unidades) { }
    public record EvolucionMensual(String mes, long ventas, long aportesConfirmados) { }
    public record MovimientoPersonal(String concepto, LocalDate fecha, long monto, String estado) { }
    public record VentaPersonal(Integer numero, LocalDate fecha, String cliente, long total,
                                int cantidadProductos, String estado) { }
    public record Resumen(LocalDate desde, LocalDate hasta, long aportesConfirmados, long aportesPendientes,
                          long ventas, int cantidadVentas, int cantidadAportesConfirmados,
                          int cantidadAportesPendientes,
                          List<ProductoVendido> productosVendidos, List<EvolucionMensual> evolucionMensual,
                          List<VentaPersonal> detalleVentas, List<MovimientoPersonal> historial) { }
}
