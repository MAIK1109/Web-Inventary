package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.CambioAlertaInventarioEntity;
import com.jdc.webinventary.entity.EstadoAlertaInventarioEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.repository.CambioAlertaInventarioRepository;
import com.jdc.webinventary.repository.EstadoAlertaInventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EstadoAlertaInventarioService {
    private final EstadoAlertaInventarioRepository estados;
    private final CambioAlertaInventarioRepository cambios;

    public EstadoAlertaInventarioService(EstadoAlertaInventarioRepository estados,
                                         CambioAlertaInventarioRepository cambios) {
        this.estados = estados;
        this.cambios = cambios;
    }

    @Transactional
    public void registrarEstadoActual(ProductosEntity producto) {
        String nuevoEstado = clasificar(producto);
        LocalDateTime ahora = LocalDateTime.now();
        EstadoAlertaInventarioEntity estado = estados.findByProducto_IdProducto(producto.getIdProducto()).orElse(null);
        if (estado == null) {
            estados.save(new EstadoAlertaInventarioEntity(producto, nuevoEstado, ahora));
        } else if (!nuevoEstado.equals(estado.getEstado())) {
            cambios.save(new CambioAlertaInventarioEntity(producto, estado.getEstado(), nuevoEstado, ahora));
            estado.setEstado(nuevoEstado);
            estado.setActualizadoEn(ahora);
        }
    }

    public String clasificar(ProductosEntity producto) {
        int stock = producto.getStockActual() == null ? 0 : producto.getStockActual();
        if (stock <= 0) return "AGOTADO";
        Integer minimo = producto.getStockMinimo();
        if (minimo != null && minimo >= 1 && stock <= minimo) return "STOCK_BAJO";
        return "DISPONIBLE";
    }
}
