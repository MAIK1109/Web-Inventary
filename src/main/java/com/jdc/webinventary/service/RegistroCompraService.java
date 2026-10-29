package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.DetalleCompraForm;
import com.jdc.webinventary.dto.RegistroCompraForm;
import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistroCompraService {
    private static final byte CONFIRMADA = 1;
    private final ComprasRepository compras;
    private final ProveedoresRepository proveedores;
    private final ProductosRepository productos;
    private final UsuariosRepository usuarios;
    private final MovimientosInventariosRepository movimientos;
    private final EstadoAlertaInventarioService estadoAlertas;
    private final AuditoriaService auditoria;

    public RegistroCompraService(ComprasRepository compras, ProveedoresRepository proveedores,
                                 ProductosRepository productos, UsuariosRepository usuarios,
                                 MovimientosInventariosRepository movimientos,
                                 EstadoAlertaInventarioService estadoAlertas, AuditoriaService auditoria) {
        this.compras = compras;
        this.proveedores = proveedores;
        this.productos = productos;
        this.usuarios = usuarios;
        this.movimientos = movimientos;
        this.estadoAlertas = estadoAlertas;
        this.auditoria = auditoria;
    }

    public List<ComprasEntity> listar() { return compras.findAllByOrderByFechaDesc(); }

    @Transactional
    public ComprasEntity registrarYConfirmar(RegistroCompraForm form, Integer usuarioId) {
        if (form.getFecha() == null || form.getProveedorId() == null || form.getDetalles() == null
                || form.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Completa la fecha, el proveedor y al menos un detalle.");
        }
        ProveedoresEntity proveedor = proveedores.findById(form.getProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));
        UsuariosEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        ComprasEntity compra = new ComprasEntity(null, form.getFecha(), CONFIRMADA, proveedor);
        List<ProductoCantidad> entradas = new ArrayList<>();

        for (DetalleCompraForm item : form.getDetalles()) {
            if (item == null || item.getProductoId() == null || item.getCantidad() == null
                    || item.getCantidad() <= 0 || item.getCostoUnitario() == null
                    || item.getCostoUnitario() < 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto, cantidad positiva y costo no negativo.");
            }
            ProductosEntity producto = productos.findByIdForUpdate(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Uno de los productos seleccionados no existe."));
            if (producto.getEstado() != null && producto.getEstado() != 1) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo.");
            }
            long stockNuevo = (long) (producto.getStockActual() == null ? 0 : producto.getStockActual()) + item.getCantidad();
            if (stockNuevo > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("La cantidad supera el stock máximo permitido para " + producto.getNombre() + ".");
            }
            producto.setStockActual((int) stockNuevo);
            estadoAlertas.registrarEstadoActual(producto);

            DetalleComprasEntity detalle = new DetalleComprasEntity();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setCostoUnitario(item.getCostoUnitario());
            compra.addDetalle(detalle);
            entradas.add(new ProductoCantidad(producto, item.getCantidad()));
        }

        compras.saveAndFlush(compra);
        for (ProductoCantidad entrada : entradas) {
            movimientos.save(new MovimientosInventariosEntity(null, "ENTRADA_COMPRA", entrada.cantidad(),
                    "COMPRA-" + compra.getIdCompra(), form.getFecha(), usuario, entrada.producto()));
        }
        auditoria.registrar("COMPRA_CONFIRMADA", "COMPRA#" + compra.getIdCompra(), usuarioId);
        return compra;
    }

    private record ProductoCantidad(ProductosEntity producto, int cantidad) { }
}
