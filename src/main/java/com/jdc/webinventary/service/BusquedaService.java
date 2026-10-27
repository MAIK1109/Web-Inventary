package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusquedaService {
    private final ProductosRepository productos;
    private final VentasRepository ventas;
    private final ComprasRepository compras;
    private final InversionesRepository inversiones;
    private final MovimientosInventariosRepository movimientos;

    public BusquedaService(ProductosRepository productos, VentasRepository ventas,
                           ComprasRepository compras, InversionesRepository inversiones,
                           MovimientosInventariosRepository movimientos) {
        this.productos = productos;
        this.ventas = ventas;
        this.compras = compras;
        this.inversiones = inversiones;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<ProductosEntity> buscarProductos(String termino) {
        if (termino == null || termino.isBlank()) return productos.findAll(Sort.by("nombre").ascending());
        String filtro = termino.trim().toLowerCase();
        Specification<ProductosEntity> criterio = (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("codigo")), "%" + filtro + "%"),
                cb.like(cb.lower(root.get("nombre")), "%" + filtro + "%"),
                cb.like(cb.lower(root.get("categoriaProducto")), "%" + filtro + "%"));
        return productos.findAll(criterio, Sort.by("nombre").ascending());
    }

    @Transactional(readOnly = true)
    public List<FilaTransaccion> buscarTransacciones(LocalDate desde, LocalDate hasta,
                                                     Integer usuarioId, Byte estado) {
        List<FilaTransaccion> filas = new ArrayList<>();
        for (VentasEntity venta : ventas.findAllByOrderByFechaDesc()) {
            if (coincide(venta.getFecha(), desde, hasta) && (usuarioId == null ||
                    venta.getVendedor() != null && usuarioId.equals(venta.getVendedor().getIdUsuario()))
                    && coincideEstado(venta.getEstado(), estado)) {
                String detalle = venta.getDetalles().stream().map(d -> d.getProducto().getNombre())
                        .distinct().reduce((a, b) -> a + ", " + b).orElse("Venta");
                filas.add(new FilaTransaccion("Venta", venta.getIdVenta(), venta.getFecha(),
                        venta.getVendedor() == null ? "Sin usuario" : venta.getVendedor().getNombre(),
                        textoEstado(venta.getEstado()), detalle));
            }
        }
        for (ComprasEntity compra : compras.findAllByOrderByFechaDesc()) {
            if (coincide(compra.getFecha(), desde, hasta) && usuarioId == null && coincideEstado(compra.getEstado(), estado)) {
                String detalle = compra.getDetalles().stream().map(d -> d.getProducto().getNombre())
                        .distinct().reduce((a, b) -> a + ", " + b).orElse("Compra");
                filas.add(new FilaTransaccion("Compra", compra.getIdCompra(), compra.getFecha(),
                        compra.getProveedor().getNombre(), textoEstado(compra.getEstado()), detalle));
            }
        }
        for (InversionesEntity aporte : inversiones.findAllByOrderByFechaDesc()) {
            if (coincide(aporte.getFecha(), desde, hasta) && (usuarioId == null ||
                    usuarioId.equals(aporte.getUsuario().getIdUsuario())) && coincideEstado(aporte.getEstado(), estado)) {
                filas.add(new FilaTransaccion("Aporte", aporte.getIdInversion(), aporte.getFecha(),
                        aporte.getUsuario().getNombre(), textoEstado(aporte.getEstado()), "Monto: " + aporte.getMonto()));
            }
        }
        for (MovimientosInventariosEntity movimiento : movimientos.findAllByOrderByFechaDesc()) {
            if (coincide(movimiento.getFecha(), desde, hasta) && (usuarioId == null ||
                    usuarioId.equals(movimiento.getUsuario().getIdUsuario())) && estado == null) {
                filas.add(new FilaTransaccion("Movimiento", movimiento.getIdMovimientoInventario(),
                        movimiento.getFecha(), movimiento.getUsuario().getNombre(), "Sin estado",
                        movimiento.getTipo() + " " + movimiento.getCantidad() + " · " + movimiento.getProducto().getNombre()));
            }
        }
        filas.sort((a, b) -> {
            if (a.fecha() == null) return b.fecha() == null ? 0 : 1;
            if (b.fecha() == null) return -1;
            return b.fecha().compareTo(a.fecha());
        });
        return filas;
    }

    private boolean coincide(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return fecha != null && (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
    }
    private boolean coincideEstado(Byte actual, Byte filtro) { return filtro == null || filtro.equals(actual); }
    private String textoEstado(Byte estado) { return estado == null ? "Sin estado" : switch (estado) {
        case 0 -> "Pendiente";
        case 1 -> "Confirmado / aprobado";
        case 2 -> "Rechazado";
        default -> estado.toString();
    }; }

    public record FilaTransaccion(String tipo, Integer id, LocalDate fecha, String usuario,
                                  String estado, String detalle) { }
}
