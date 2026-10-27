package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.DetalleVentasEntity;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.repository.VentasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardComercialService {
    private final VentasRepository ventasRepository;

    public DashboardComercialService(VentasRepository ventasRepository) {
        this.ventasRepository = ventasRepository;
    }

    @Transactional(readOnly = true)
    public Resumen generar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El período seleccionado no es válido.");
        }
        List<VentasEntity> ventas = ventasRepository.findByFechaBetweenOrderByFechaAsc(desde, hasta)
                .stream().filter(v -> v.getEstado() == null || v.getEstado() == 1).toList();

        Map<LocalDate, Long> porFecha = new TreeMap<>();
        Map<String, Long> productos = new LinkedHashMap<>();
        Map<String, Long> vendedores = new LinkedHashMap<>();
        Map<String, Long> clientes = new LinkedHashMap<>();
        Map<String, Map<String, Long>> productosPorCliente = new LinkedHashMap<>();
        long total = 0;

        for (VentasEntity venta : ventas) {
            String cliente = nombre(venta.getCliente().getNombre());
            long totalVenta = 0;
            for (DetalleVentasEntity detalle : venta.getDetalles()) {
                int cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
                long importe = (long) cantidad * (detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario());
                String producto = nombre(detalle.getProducto().getNombre());
                productos.merge(producto, (long) cantidad, Long::sum);
                productosPorCliente.computeIfAbsent(cliente, key -> new LinkedHashMap<>())
                        .merge(producto, (long) cantidad, Long::sum);
                totalVenta += importe;
            }
            total += totalVenta;
            porFecha.merge(venta.getFecha(), totalVenta, Long::sum);
            if (venta.getVendedor() != null) {
                vendedores.merge(nombre(venta.getVendedor().getNombre()), totalVenta, Long::sum);
            }
            clientes.merge(cliente, totalVenta, Long::sum);
        }

        Map<String, String> masCompradoPorCliente = new LinkedHashMap<>();
        productosPorCliente.forEach((cliente, cantidades) -> masCompradoPorCliente.put(cliente,
                cantidades.entrySet().stream().max(Map.Entry.comparingByValue())
                        .map(e -> e.getKey() + " (" + e.getValue() + ")").orElse("—")));

        return new Resumen(desde, hasta, ventas.size(), total, porFecha,
                ranking(productos), ranking(vendedores), ranking(clientes), masCompradoPorCliente);
    }

    private List<Indicador> ranking(Map<String, Long> valores) {
        return valores.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new Indicador(entry.getKey(), entry.getValue())).toList();
    }

    private String nombre(String valor) { return valor == null || valor.isBlank() ? "Sin nombre" : valor; }

    public record Indicador(String nombre, long valor) { }
    public record Resumen(LocalDate desde, LocalDate hasta, int cantidadVentas, long totalVentas,
                          Map<LocalDate, Long> ventasPorFecha, List<Indicador> productosMasVendidos,
                          List<Indicador> vendedoresTop, List<Indicador> clientesTop,
                          Map<String, String> productoMasCompradoPorCliente) { }
}
