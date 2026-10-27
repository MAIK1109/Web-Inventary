package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.DetalleVentaForm;
import com.jdc.webinventary.entity.ClientesEntity;
import com.jdc.webinventary.entity.DetalleVentasEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.repository.ClientesRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import com.jdc.webinventary.repository.VentasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistroVentaService {

    private final ClientesRepository clientesRepository;
    private final ProductosRepository productosRepository;
    private final VentasRepository ventasRepository;

    public RegistroVentaService(ClientesRepository clientesRepository,
                                ProductosRepository productosRepository,
                                VentasRepository ventasRepository) {
        this.clientesRepository = clientesRepository;
        this.productosRepository = productosRepository;
        this.ventasRepository = ventasRepository;
    }

    @Transactional
    public VentasEntity registrar(Integer clienteId, List<DetalleVentaForm> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto.");
        }

        ClientesEntity cliente = clientesRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));
        VentasEntity venta = new VentasEntity(null, LocalDate.now(), (byte) 1, cliente);

        for (DetalleVentaForm item : detalles) {
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }

            ProductosEntity producto = productosRepository.findByIdForUpdate(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
            if (producto.getStockActual() == null || producto.getStockActual() < item.getCantidad()) {
                throw new StockInsuficienteException(producto.getNombre());
            }
            if (productosRepository.descontarStockSiDisponible(producto.getIdProducto(), item.getCantidad()) != 1) {
                throw new StockInsuficienteException(producto.getNombre());
            }

            DetalleVentasEntity detalle = new DetalleVentasEntity(
                    null, item.getCantidad(), item.getPrecioUnitario(),
                    item.getCostoUnitarioHistorico(), item.getMedioPago(), venta, producto);
            venta.addDetalle(detalle);
        }

        return ventasRepository.save(venta);
    }

    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String producto) {
            super("Stock insuficiente para el producto: " + producto + ".");
        }
    }
}
