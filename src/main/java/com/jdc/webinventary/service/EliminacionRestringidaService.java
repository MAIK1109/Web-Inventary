package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class EliminacionRestringidaService {
    private final AnulacionesRepository anulaciones;
    private final AuditoriasRepository auditorias;
    private final ComprasRepository compras;
    private final VentasRepository ventas;
    private final ProductosRepository productos;
    private final ClientesRepository clientes;
    private final ProveedoresRepository proveedores;
    private final GastosRepository gastos;
    private final InversionesRepository inversiones;
    private final MovimientosInventariosRepository movimientos;
    private final EstadoAlertaInventarioRepository estadosAlerta;
    private final CambioAlertaInventarioRepository cambiosAlerta;
    private final UsuariosRepository usuarios;
    private final EstadoAlertaInventarioService estadoAlertas;

    public EliminacionRestringidaService(AnulacionesRepository anulaciones, AuditoriasRepository auditorias,
            ComprasRepository compras, VentasRepository ventas, ProductosRepository productos,
            ClientesRepository clientes, ProveedoresRepository proveedores, GastosRepository gastos,
            InversionesRepository inversiones, MovimientosInventariosRepository movimientos,
            EstadoAlertaInventarioRepository estadosAlerta, CambioAlertaInventarioRepository cambiosAlerta,
            UsuariosRepository usuarios, EstadoAlertaInventarioService estadoAlertas) {
        this.anulaciones = anulaciones; this.auditorias = auditorias; this.compras = compras;
        this.ventas = ventas; this.productos = productos; this.clientes = clientes;
        this.proveedores = proveedores; this.gastos = gastos; this.inversiones = inversiones;
        this.movimientos = movimientos; this.estadosAlerta = estadosAlerta;
        this.cambiosAlerta = cambiosAlerta; this.usuarios = usuarios; this.estadoAlertas = estadoAlertas;
    }

    @Transactional(readOnly = true)
    public java.util.List<AnulacionesEntity> historial() { return anulaciones.findAllByOrderByFechaAnulacionDesc(); }

    @Transactional
    public void anular(String tipo, Integer id, String motivo, Integer usuarioId) {
        if (id == null || motivo == null || motivo.isBlank() || motivo.trim().length() > 250)
            throw new IllegalArgumentException("El identificador y un motivo de hasta 250 caracteres son obligatorios.");
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase();
        UsuariosEntity actor = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        switch (tipoNormalizado) {
            case "VENTA" -> anularVenta(id, motivo.trim(), actor);
            case "COMPRA" -> anularCompra(id, motivo.trim(), actor);
            case "GASTO" -> cambiarEstado("GASTO", id, motivo.trim(), actor, () -> {
                GastosEntity e = gastos.findById(id).orElseThrow(() -> noExiste("gasto"));
                comprobarActivo(e.getEstado()); e.setEstado((byte) 3);
            });
            case "APORTE" -> cambiarEstado("APORTE", id, motivo.trim(), actor, () -> {
                InversionesEntity e = inversiones.findById(id).orElseThrow(() -> noExiste("aporte"));
                if (e.getEstado() == null || e.getEstado() != 1) throw new IllegalArgumentException("Solo se pueden anular aportes aprobados.");
                e.setEstado((byte) 3);
            });
            default -> throw new IllegalArgumentException("Tipo de operación no permitido.");
        }
    }

    private void anularVenta(Integer id, String motivo, UsuariosEntity actor) {
        if (anulaciones.existsByTipoOperacionAndOperacionId("VENTA", id)) throw new IllegalArgumentException("La venta ya fue anulada.");
        VentasEntity venta = ventas.buscarParaAnular(id).orElseThrow(() -> noExiste("venta"));
        comprobarActivo(venta.getEstado());
        for (DetalleVentasEntity detalle : venta.getDetalles()) {
            ProductosEntity p = productos.findByIdForUpdate(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró un producto de la venta."));
            long nuevo = (long) (p.getStockActual() == null ? 0 : p.getStockActual()) + detalle.getCantidad();
            if (nuevo > Integer.MAX_VALUE) throw new IllegalArgumentException("La reversión supera el stock máximo de " + p.getNombre() + ".");
            p.setStockActual((int) nuevo);
            estadoAlertas.registrarEstadoActual(p);
            movimientos.save(new MovimientosInventariosEntity(null, "ANULACION_VENTA", detalle.getCantidad(),
                    "ANULACION-VENTA-" + id, LocalDate.now(), actor, p));
        }
        venta.setEstado((byte) 3);
        registrar("VENTA", id, motivo, actor);
    }

    private void anularCompra(Integer id, String motivo, UsuariosEntity actor) {
        if (anulaciones.existsByTipoOperacionAndOperacionId("COMPRA", id)) throw new IllegalArgumentException("La compra ya fue anulada.");
        ComprasEntity compra = compras.buscarParaAnular(id).orElseThrow(() -> noExiste("compra"));
        comprobarActivo(compra.getEstado());
        for (DetalleComprasEntity detalle : compra.getDetalles()) {
            ProductosEntity p = productos.findByIdForUpdate(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró un producto de la compra."));
            int stock = p.getStockActual() == null ? 0 : p.getStockActual();
            if (stock < detalle.getCantidad()) throw new IllegalArgumentException("No se puede anular: el stock actual de " + p.getNombre() + " ya no contiene las unidades de esta compra.");
            p.setStockActual(stock - detalle.getCantidad());
            estadoAlertas.registrarEstadoActual(p);
            movimientos.save(new MovimientosInventariosEntity(null, "ANULACION_COMPRA", detalle.getCantidad(),
                    "ANULACION-COMPRA-" + id, LocalDate.now(), actor, p));
        }
        compra.setEstado((byte) 3);
        registrar("COMPRA", id, motivo, actor);
    }

    private void cambiarEstado(String tipo, Integer id, String motivo, UsuariosEntity actor, Runnable cambio) {
        if (anulaciones.existsByTipoOperacionAndOperacionId(tipo, id)) throw new IllegalArgumentException("La operación ya fue anulada.");
        cambio.run(); registrar(tipo, id, motivo, actor);
    }

    private void registrar(String tipo, Integer id, String motivo, UsuariosEntity actor) {
        anulaciones.save(new AnulacionesEntity(tipo, id, LocalDateTime.now(), motivo, actor));
    }

    private void comprobarActivo(Byte estado) {
        if (estado != null && estado != 1) throw new IllegalArgumentException("Solo se pueden anular operaciones confirmadas.");
    }
    private IllegalArgumentException noExiste(String tipo) { return new IllegalArgumentException("No existe el " + tipo + "."); }

    @Transactional
    public void eliminarMaestro(String tipo, Integer id, Integer usuarioId) {
        UsuariosEntity actor = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase();
        switch (tipoNormalizado) {
            case "PRODUCTO" -> {
                ProductosEntity p = productos.findById(id).orElseThrow(() -> noExiste("producto"));
                if (productos.tieneDetallesCompra(id) || productos.tieneDetallesVenta(id)
                        || movimientos.existsByProducto_IdProducto(id) || estadosAlerta.existsByProducto_IdProducto(id)
                        || cambiosAlerta.existsByProducto_IdProducto(id))
                    throw new IllegalArgumentException("El producto tiene movimientos o historial asociado y no se puede eliminar.");
                auditar("PRODUCTO#" + id + ":" + p.getCodigo(), actor); productos.delete(p); productos.flush();
            }
            case "CLIENTE" -> {
                ClientesEntity c = clientes.findById(id).orElseThrow(() -> noExiste("cliente"));
                if (ventas.existsByCliente_IdCliente(id)) throw new IllegalArgumentException("El cliente tiene ventas asociadas y no se puede eliminar.");
                auditar("CLIENTE#" + id + ":" + c.getIdentificacion(), actor); clientes.delete(c); clientes.flush();
            }
            case "PROVEEDOR" -> {
                ProveedoresEntity p = proveedores.findById(id).orElseThrow(() -> noExiste("proveedor"));
                if (compras.existsByProveedor_IdProveedor(id)) throw new IllegalArgumentException("El proveedor tiene compras asociadas y no se puede eliminar.");
                auditar("PROVEEDOR#" + id + ":" + p.getIdentificacion(), actor); proveedores.delete(p); proveedores.flush();
            }
            default -> throw new IllegalArgumentException("Tipo de registro no permitido para eliminación.");
        }
    }

    private void auditar(String entidad, UsuariosEntity actor) {
        String referencia = entidad.length() > 45 ? entidad.substring(0, 45) : entidad;
        auditorias.save(new AuditoriasEntity(null, "ELIMINACION", referencia, LocalDate.now(), actor));
    }
}
