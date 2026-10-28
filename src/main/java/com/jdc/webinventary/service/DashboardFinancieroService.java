package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.RegistroGastoForm;
import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardFinancieroService {
    private static final byte CONFIRMADA = 1;
    private final VentasRepository ventas;
    private final ComprasRepository compras;
    private final GastosRepository gastos;
    private final UsuariosRepository usuarios;
    private final GastoComprobanteStorageService comprobantes;

    public DashboardFinancieroService(VentasRepository ventas, ComprasRepository compras,
                                      GastosRepository gastos, UsuariosRepository usuarios,
                                      GastoComprobanteStorageService comprobantes) {
        this.ventas = ventas;
        this.compras = compras;
        this.gastos = gastos;
        this.usuarios = usuarios;
        this.comprobantes = comprobantes;
    }

    @Transactional(readOnly = true)
    public Resumen generar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El período seleccionado no es válido.");
        }
        List<VentasEntity> ventasPeriodo = ventas.findByFechaBetweenOrderByFechaAsc(desde, hasta)
                .stream().filter(v -> v.getEstado() == null || v.getEstado() == CONFIRMADA).toList();
        List<ComprasEntity> historialCostos = compras.findByFechaLessThanEqualAndEstadoOrderByFechaDesc(hasta, CONFIRMADA);
        Map<Integer, List<CostoFecha>> costosPorProducto = indexarCostos(historialCostos);
        List<GastosEntity> gastosPeriodo = gastos.findByFechaBetweenOrderByFechaAsc(desde, hasta);

        Map<YearMonth, long[]> meses = new TreeMap<>();
        YearMonth mes = YearMonth.from(desde);
        YearMonth ultimoMes = YearMonth.from(hasta);
        while (!mes.isAfter(ultimoMes)) {
            meses.put(mes, new long[4]); // ventas, costo, gastos, detalles sin costo
            mes = mes.plusMonths(1);
        }

        long ventasNetas = 0;
        long costoMercancia = 0;
        long gastosTotales = 0;
        int lineasSinCosto = 0;
        for (VentasEntity venta : ventasPeriodo) {
            long ingresoVenta = 0;
            long costoVenta = 0;
            for (DetalleVentasEntity detalle : venta.getDetalles()) {
                long cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
                int precio = detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario();
                ingresoVenta = Math.addExact(ingresoVenta, Math.multiplyExact(cantidad, precio));
                Integer costoUnitario = detalle.getCostoUnitarioHistorico();
                if (costoUnitario == null) {
                    costoUnitario = costoAFecha(costosPorProducto.get(detalle.getProducto().getIdProducto()), venta.getFecha());
                }
                if (costoUnitario == null) {
                    lineasSinCosto++;
                    meses.get(YearMonth.from(venta.getFecha()))[3]++;
                } else {
                    costoVenta = Math.addExact(costoVenta, Math.multiplyExact(cantidad, costoUnitario));
                }
            }
            ventasNetas = Math.addExact(ventasNetas, ingresoVenta);
            costoMercancia = Math.addExact(costoMercancia, costoVenta);
            long[] filaMes = meses.get(YearMonth.from(venta.getFecha()));
            filaMes[0] = Math.addExact(filaMes[0], ingresoVenta);
            filaMes[1] = Math.addExact(filaMes[1], costoVenta);
        }
        for (GastosEntity gasto : gastosPeriodo) {
            gastosTotales = Math.addExact(gastosTotales, gasto.getMonto());
            long[] filaMes = meses.get(YearMonth.from(gasto.getFecha()));
            filaMes[2] = Math.addExact(filaMes[2], gasto.getMonto());
        }

        long utilidadBruta = ventasNetas - costoMercancia;
        long utilidadNeta = utilidadBruta - gastosTotales;
        double margenNeto = ventasNetas == 0 ? 0 : (utilidadNeta * 100.0) / ventasNetas;
        List<EvolucionMensual> evolucion = meses.entrySet().stream().map(entry -> {
            long[] valores = entry.getValue();
            return new EvolucionMensual(entry.getKey().toString(), valores[0], valores[1], valores[2],
                    valores[0] - valores[1] - valores[2]);
        }).toList();

        return new Resumen(desde, hasta, ventasNetas, costoMercancia, utilidadBruta,
                gastosTotales, utilidadNeta, margenNeto, ventasPeriodo.size(),
                lineasSinCosto, evolucion, gastosPeriodo);
    }

    @Transactional
    public void registrarGasto(RegistroGastoForm form, Integer usuarioId) {
        if (form.getMonto() == null || form.getMonto() <= 0 || form.getFecha() == null
                || form.getConcepto() == null || form.getConcepto().isBlank()
                || form.getCategoria() == null || form.getCategoria().isBlank()) {
            throw new IllegalArgumentException("Completa el monto, la fecha, la categoría y el concepto del gasto.");
        }
        UsuariosEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        String comprobante = comprobantes.guardar(form.getComprobante());
        try {
            GastosEntity gasto = new GastosEntity(form.getMonto(), form.getFecha(), form.getConcepto().trim(),
                    form.getCategoria().trim(), usuario);
            gasto.setComprobanteArchivo(comprobante);
            gastos.saveAndFlush(gasto);
        } catch (RuntimeException ex) {
            comprobantes.eliminar(comprobante);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Resource comprobanteParaDescarga(Integer gastoId) {
        GastosEntity gasto = gastos.findById(gastoId).orElseThrow(GastoNoEncontradoException::new);
        if (gasto.getComprobanteArchivo() == null) throw new GastoNoEncontradoException();
        try {
            return comprobantes.cargar(gasto.getComprobanteArchivo());
        } catch (GastoComprobanteStorageService.ComprobanteNoEncontradoException ex) {
            throw new GastoNoEncontradoException();
        }
    }

    private Map<Integer, List<CostoFecha>> indexarCostos(List<ComprasEntity> comprasConfirmadas) {
        Map<Integer, List<CostoFecha>> mapa = new HashMap<>();
        for (ComprasEntity compra : comprasConfirmadas) {
            if (compra.getFecha() == null) continue;
            for (DetalleComprasEntity detalle : compra.getDetalles()) {
                if (detalle.getCostoUnitario() == null || detalle.getProducto() == null) continue;
                mapa.computeIfAbsent(detalle.getProducto().getIdProducto(), key -> new ArrayList<>())
                        .add(new CostoFecha(compra.getFecha(), compra.getIdCompra(),
                                detalle.getIdDetalleCompra(), detalle.getCostoUnitario()));
            }
        }
        mapa.values().forEach(costos -> costos.sort(Comparator.comparing(CostoFecha::fecha).reversed()
                .thenComparing(CostoFecha::compraId, Comparator.reverseOrder())
                .thenComparing(CostoFecha::detalleId, Comparator.reverseOrder())));
        return mapa;
    }

    private Integer costoAFecha(List<CostoFecha> costos, LocalDate fechaVenta) {
        if (costos == null || fechaVenta == null) return null;
        return costos.stream().filter(c -> !c.fecha().isAfter(fechaVenta))
                .findFirst().map(CostoFecha::costo).orElse(null);
    }

    private record CostoFecha(LocalDate fecha, Integer compraId, Integer detalleId, Integer costo) { }
    public static class GastoNoEncontradoException extends RuntimeException { }
    public record EvolucionMensual(String mes, long ventasNetas, long costoMercancia,
                                   long gastos, long utilidadNeta) { }
    public record Resumen(LocalDate desde, LocalDate hasta, long ventasNetas,
                          long costoMercanciaVendida, long utilidadBruta, long gastos,
                          long utilidadNetaEstimada, double margenNetoPorcentaje,
                          int cantidadVentas, int lineasSinCosto, List<EvolucionMensual> evolucionMensual,
                          List<GastosEntity> gastosDetalle) { }
}
