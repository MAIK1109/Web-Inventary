package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.DetalleVentasEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.repository.ClientesRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import com.jdc.webinventary.repository.VentasRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportesService {
    private final VentasRepository ventas;
    private final ProductosRepository productos;
    private final ClientesRepository clientes;
    private final UsuariosRepository usuarios;

    public ReportesService(VentasRepository ventas, ProductosRepository productos,
                           ClientesRepository clientes, UsuariosRepository usuarios) {
        this.ventas = ventas; this.productos = productos; this.clientes = clientes; this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Resultado filtrar(LocalDate desde, LocalDate hasta, Integer productoId,
                             Integer clienteId, Integer vendedorId, Integer usuarioActual,
                             boolean puedeVerTodos) {
        if (desde == null || hasta == null || desde.isAfter(hasta))
            throw new IllegalArgumentException("Selecciona un período válido: la fecha inicial debe ser anterior o igual a la final.");
        if (productoId != null && !productos.existsById(productoId)) throw new IllegalArgumentException("El producto seleccionado no existe.");
        if (clienteId != null && !clientes.existsById(clienteId)) throw new IllegalArgumentException("El cliente seleccionado no existe.");
        if (puedeVerTodos && vendedorId != null && !usuarios.existsById(vendedorId)) throw new IllegalArgumentException("El vendedor seleccionado no existe.");

        Integer vendedorEfectivo = puedeVerTodos ? vendedorId : usuarioActual;
        List<Fila> filas = new ArrayList<>();
        for (VentasEntity venta : ventas.findAllByOrderByFechaDesc()) {
            if (venta.getFecha() == null || venta.getFecha().isBefore(desde) || venta.getFecha().isAfter(hasta)) continue;
            if (clienteId != null && !clienteId.equals(venta.getCliente().getIdCliente())) continue;
            Integer propietario = venta.getVendedor() == null ? null : venta.getVendedor().getIdUsuario();
            if (vendedorEfectivo != null && !vendedorEfectivo.equals(propietario)) continue;
            for (DetalleVentasEntity detalle : venta.getDetalles()) {
                ProductosEntity producto = detalle.getProducto();
                if (productoId != null && !productoId.equals(producto.getIdProducto())) continue;
                int cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
                int precio = detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario();
                long total = Math.multiplyExact((long) cantidad, precio);
                filas.add(new Fila(venta.getIdVenta(), venta.getFecha(),
                        venta.getCliente().getNombre(), producto.getCodigo(), producto.getNombre(),
                        venta.getVendedor() == null ? "Sin asignar" : venta.getVendedor().getNombre(),
                        cantidad, precio, total, estado(venta.getEstado())));
            }
        }
        long totalConfirmado = filas.stream().filter(f -> "CONFIRMADA".equals(f.estado())).mapToLong(Fila::total).sum();
        long anuladas = filas.stream().filter(f -> "ANULADA".equals(f.estado())).count();
        return new Resultado(desde, hasta, filas, totalConfirmado, anuladas);
    }

    public List<ProductosEntity> listarProductos() { return productos.findAll(Sort.by("nombre").ascending()); }
    public List<com.jdc.webinventary.entity.ClientesEntity> listarClientes() { return clientes.findAll(Sort.by("nombre").ascending()); }
    public List<com.jdc.webinventary.entity.UsuariosEntity> listarVendedores() { return usuarios.findAll(Sort.by("nombre").ascending()); }

    private String estado(Byte estado) {
        if (estado == null || estado == 1) return "CONFIRMADA";
        if (estado == 3) return "ANULADA";
        return "ESTADO " + estado;
    }

    public record Fila(Integer ventaId, LocalDate fecha, String cliente, String codigoProducto,
                       String producto, String vendedor, int cantidad, int precioUnitario,
                       long total, String estado) { }
    public record Resultado(LocalDate desde, LocalDate hasta, List<Fila> filas,
                            long totalConfirmado, long lineasAnuladas) { }
}
