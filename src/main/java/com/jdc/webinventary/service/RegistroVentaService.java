package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.DetalleVentaForm;
import com.jdc.webinventary.dto.RegistroVentaForm;
import com.jdc.webinventary.entity.*;
import com.jdc.webinventary.repository.ClientesRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import com.jdc.webinventary.repository.VentasRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import com.jdc.webinventary.repository.MovimientosInventariosRepository;
import com.jdc.webinventary.repository.ComprasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistroVentaService {

    private final ClientesRepository clientesRepository;
    private final ProductosRepository productosRepository;
    private final VentasRepository ventasRepository;
    private final UsuariosRepository usuariosRepository;
    private final MovimientosInventariosRepository movimientosRepository;
    private final EstadoAlertaInventarioService estadoAlertas;
    private final EntityManager entityManager;
    private final ComprasRepository comprasRepository;
    private final CorreoEventosService correoEventos;

    public RegistroVentaService(ClientesRepository clientesRepository,
                                ProductosRepository productosRepository,
                                VentasRepository ventasRepository,
                                UsuariosRepository usuariosRepository,
                                MovimientosInventariosRepository movimientosRepository,
                                EstadoAlertaInventarioService estadoAlertas,
                                EntityManager entityManager,
                                ComprasRepository comprasRepository,
                                CorreoEventosService correoEventos) {
        this.clientesRepository = clientesRepository;
        this.productosRepository = productosRepository;
        this.ventasRepository = ventasRepository;
        this.usuariosRepository = usuariosRepository;
        this.movimientosRepository = movimientosRepository;
        this.estadoAlertas = estadoAlertas;
        this.entityManager = entityManager;
        this.comprasRepository = comprasRepository;
        this.correoEventos = correoEventos;
    }

    @Transactional
    public VentasEntity registrar(Integer clienteId, Integer vendedorId, List<DetalleVentaForm> detalles) {
        return registrarInterno(clienteId, vendedorId, detalles, false);
    }

    @Transactional
    public VentasEntity registrar(RegistroVentaForm form, Integer vendedorId) {
        if (form.getClienteId() == null || form.getMedioPago() == null || form.getDetalles() == null
                || form.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Completa el cliente, el medio de pago y al menos un producto.");
        }
        for (DetalleVentaForm item : form.getDetalles()) {
            if (item != null) item.setMedioPago(form.getMedioPago().trim());
        }
        return registrarInterno(form.getClienteId(), vendedorId, form.getDetalles(), true);
    }

    private VentasEntity registrarInterno(Integer clienteId, Integer vendedorId,
                                          List<DetalleVentaForm> detalles, boolean usarPrecioCatalogo) {
        if (vendedorId == null) {
            throw new IllegalArgumentException("La venta debe estar asociada al vendedor que la registra.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto.");
        }

        ClientesEntity cliente = clientesRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
        VentasEntity venta = new VentasEntity(null, LocalDate.now(), (byte) 1, cliente);
        if (vendedorId != null) {
            venta.setVendedor(usuariosRepository.findById(vendedorId)
                    .orElseThrow(() -> new IllegalArgumentException("El vendedor no existe.")));
        }
        long total = 0;

        for (DetalleVentaForm item : detalles) {
            if (item == null || item.getProductoId() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto y cantidad mayor que cero.");
            }

            ProductosEntity producto = productosRepository.findByIdForUpdate(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
            if (producto.getEstado() != null && producto.getEstado() != 1) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo.");
            }
            if (producto.getStockActual() == null || producto.getStockActual() < item.getCantidad()) {
                throw new StockInsuficienteException(producto.getNombre());
            }
            if (productosRepository.descontarStockSiDisponible(producto.getIdProducto(), item.getCantidad()) != 1) {
                throw new StockInsuficienteException(producto.getNombre());
            }
            entityManager.refresh(producto, LockModeType.PESSIMISTIC_WRITE);
            estadoAlertas.registrarEstadoActual(producto);

            Integer precio = usarPrecioCatalogo ? producto.getPrecioVenta() : item.getPrecioUnitario();
            if (precio == null || precio < 0) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene un precio de venta válido.");
            }
            try {
                total = Math.addExact(total, Math.multiplyExact((long) item.getCantidad(), precio));
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("El total de la venta excede el valor máximo permitido.");
            }

            DetalleVentasEntity detalle = new DetalleVentasEntity(
                    null, item.getCantidad(), precio,
                    usarPrecioCatalogo ? comprasRepository.costoUnitarioMasReciente(producto.getIdProducto(), venta.getFecha())
                            : item.getCostoUnitarioHistorico(), item.getMedioPago(), venta, producto);
            venta.addDetalle(detalle);
        }

        VentasEntity guardada = ventasRepository.saveAndFlush(venta);
        for (DetalleVentasEntity detalle : guardada.getDetalles()) {
            movimientosRepository.save(new MovimientosInventariosEntity(null, "SALIDA_VENTA", detalle.getCantidad(),
                    "VENTA-" + guardada.getIdVenta(), guardada.getFecha(),
                    guardada.getVendedor(), detalle.getProducto()));
        }
        correoEventos.enviarComprobanteVenta(guardada);
        return guardada;
    }

    public List<VentasEntity> listar() { return ventasRepository.findAllByOrderByFechaDesc(); }

    public long calcularTotal(VentasEntity venta) {
        long total = 0;
        for (DetalleVentasEntity detalle : venta.getDetalles()) {
            total = Math.addExact(total, Math.multiplyExact((long) detalle.getCantidad(), detalle.getPrecioUnitario()));
        }
        return total;
    }

    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String producto) {
            super("Stock insuficiente para el producto: " + producto + ".");
        }
    }
}
