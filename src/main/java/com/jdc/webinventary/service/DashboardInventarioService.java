package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.MovimientosInventariosEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.entity.CambioAlertaInventarioEntity;
import com.jdc.webinventary.repository.CambioAlertaInventarioRepository;
import com.jdc.webinventary.repository.MovimientosInventariosRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Locale;

@Service
public class DashboardInventarioService {
    private final ProductosRepository productosRepository;
    private final MovimientosInventariosRepository movimientosRepository;
    private final CambioAlertaInventarioRepository cambiosRepository;
    private final EstadoAlertaInventarioService estadoAlertas;
    private final EntityManager entityManager;

    public DashboardInventarioService(ProductosRepository productosRepository,
                                      MovimientosInventariosRepository movimientosRepository,
                                      CambioAlertaInventarioRepository cambiosRepository,
                                      EstadoAlertaInventarioService estadoAlertas,
                                      EntityManager entityManager) {
        this.productosRepository = productosRepository;
        this.movimientosRepository = movimientosRepository;
        this.cambiosRepository = cambiosRepository;
        this.estadoAlertas = estadoAlertas;
        this.entityManager = entityManager;
    }

    @Transactional
    public Resumen generar(String filtroEstado) {
        List<ProductosEntity> productos = productosRepository.findAll(Sort.by("idProducto").ascending());
        for (ProductosEntity producto : productos) {
            ProductosEntity bloqueado = productosRepository.findByIdForUpdate(producto.getIdProducto()).orElse(producto);
            entityManager.refresh(bloqueado, LockModeType.PESSIMISTIC_WRITE);
            estadoAlertas.registrarEstadoActual(bloqueado);
        }
        productos = productosRepository.findAll(Sort.by("nombre").ascending());
        List<ProductosEntity> agotados = productos.stream()
                .filter(p -> "AGOTADO".equals(estadoAlertas.clasificar(p))).toList();
        List<ProductosEntity> bajos = productos.stream()
                .filter(p -> "STOCK_BAJO".equals(estadoAlertas.clasificar(p)))
                .toList();
        int unidades = productos.stream().map(ProductosEntity::getStockActual)
                .filter(stock -> stock != null).mapToInt(Integer::intValue).sum();
        List<MovimientosInventariosEntity> recientes = movimientosRepository.findRecientes(PageRequest.of(0, 10));
        String filtro = normalizarFiltro(filtroEstado);
        List<ProductosEntity> filtrados = productos.stream()
                .filter(p -> "TODOS".equals(filtro) || filtro.equals(estadoAlertas.clasificar(p))).toList();
        List<CambioAlertaInventarioEntity> cambios = cambiosRepository.findRecientes(PageRequest.of(0, 10));
        return new Resumen(filtrados, productos.size(), agotados, bajos, unidades, recientes,
                cambios, filtro);
    }

    public Resumen generar() { return generar("TODOS"); }

    private String normalizarFiltro(String filtro) {
        if (filtro == null || filtro.isBlank()) return "TODOS";
        String normalizado = filtro.trim().toUpperCase(Locale.ROOT);
        return List.of("TODOS", "AGOTADO", "STOCK_BAJO", "DISPONIBLE").contains(normalizado)
                ? normalizado : "TODOS";
    }

    public record Resumen(List<ProductosEntity> productos, int totalProductos,
                          List<ProductosEntity> agotados, List<ProductosEntity> bajos,
                          int unidadesEnExistencia, List<MovimientosInventariosEntity> movimientosRecientes,
                          List<CambioAlertaInventarioEntity> cambiosAlerta, String filtroEstado) { }
}
