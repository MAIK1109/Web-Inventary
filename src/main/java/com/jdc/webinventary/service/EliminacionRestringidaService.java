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
    private final AuditoriaService auditoria;
    private final ComprasRepository compras;
    private final VentasRepository ventas;
    private final ProductosRepository productos;
    private final ClientesRepository clientes;
    private final ProveedoresRepository proveedores;
    private final MovimientosInventariosRepository movimientos;
    private final EstadoAlertaInventarioRepository estadosAlerta;
    private final CambioAlertaInventarioRepository cambiosAlerta;
    private final UsuariosRepository usuarios;
    private final EstadoAlertaInventarioService estadoAlertas;

    public EliminacionRestringidaService(AnulacionesRepository anulaciones, AuditoriaService auditoria,
            ComprasRepository compras, VentasRepository ventas, ProductosRepository productos,
            ClientesRepository clientes, ProveedoresRepository proveedores,
            MovimientosInventariosRepository movimientos,
            EstadoAlertaInventarioRepository estadosAlerta, CambioAlertaInventarioRepository cambiosAlerta,
            UsuariosRepository usuarios, EstadoAlertaInventarioService estadoAlertas) {
        this.anulaciones = anulaciones; this.auditoria = auditoria; this.compras = compras;
        this.ventas = ventas; this.productos = productos; this.clientes = clientes;
        this.proveedores = proveedores;
        this.movimientos = movimientos; this.estadosAlerta = estadosAlerta;
        this.cambiosAlerta = cambiosAlerta; this.usuarios = usuarios; this.estadoAlertas = estadoAlertas;
    }

    @Transactional(readOnly = true)
    public java.util.List<AnulacionesEntity> historial() { return anulaciones.findAllByTipoOperacionOrderByFechaAnulacionDesc("VENTA"); }

    @Transactional(readOnly = true)
    public java.util.List<VentasEntity> ventasConfirmadasParaAnular() {
        return ventas.findAllByEstadoOrderByFechaDesc((byte) 1);
    }

    @Transactional
    public void anularVenta(Integer id, String motivo, Integer usuarioId) {
        if (id == null || motivo == null || motivo.isBlank() || motivo.trim().length() > 250)
            throw new IllegalArgumentException("El identificador de venta y un motivo de hasta 250 caracteres son obligatorios.");
        UsuariosEntity actor = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        aplicarAnulacionVenta(id, motivo.trim(), actor);
    }

    private void aplicarAnulacionVenta(Integer id, String motivo, UsuariosEntity actor) {
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

    private void registrar(String tipo, Integer id, String motivo, UsuariosEntity actor) {
        anulaciones.save(new AnulacionesEntity(tipo, id, LocalDateTime.now(), motivo, actor));
        auditoria.registrar("ANULACION_" + tipo, tipo + "#" + id + ":ANULADA", actor.getIdUsuario());
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
                auditoria.registrar("ELIMINACION", "PRODUCTO#" + id + ":" + p.getCodigo(), usuarioId); productos.delete(p); productos.flush();
            }
            case "CLIENTE" -> {
                ClientesEntity c = clientes.findById(id).orElseThrow(() -> noExiste("cliente"));
                if (ventas.existsByCliente_IdCliente(id)) throw new IllegalArgumentException("El cliente tiene ventas asociadas y no se puede eliminar.");
                auditoria.registrar("ELIMINACION", "CLIENTE#" + id + ":" + c.getIdentificacion(), usuarioId); clientes.delete(c); clientes.flush();
            }
            case "PROVEEDOR" -> {
                ProveedoresEntity p = proveedores.findById(id).orElseThrow(() -> noExiste("proveedor"));
                if (compras.existsByProveedor_IdProveedor(id)) throw new IllegalArgumentException("El proveedor tiene compras asociadas y no se puede eliminar.");
                auditoria.registrar("ELIMINACION", "PROVEEDOR#" + id + ":" + p.getIdentificacion(), usuarioId); proveedores.delete(p); proveedores.flush();
            }
            default -> throw new IllegalArgumentException("Tipo de registro no permitido para eliminación.");
        }
    }

}
