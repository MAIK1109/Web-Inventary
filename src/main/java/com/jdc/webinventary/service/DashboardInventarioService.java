package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.MovimientosInventariosEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.repository.MovimientosInventariosRepository;
import com.jdc.webinventary.repository.ProductosRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardInventarioService {
    private final ProductosRepository productosRepository;
    private final MovimientosInventariosRepository movimientosRepository;

    public DashboardInventarioService(ProductosRepository productosRepository,
                                      MovimientosInventariosRepository movimientosRepository) {
        this.productosRepository = productosRepository;
        this.movimientosRepository = movimientosRepository;
    }

    @Transactional(readOnly = true)
    public Resumen generar() {
        List<ProductosEntity> productos = productosRepository.findAll();
        List<ProductosEntity> agotados = productos.stream()
                .filter(p -> p.getStockActual() == null || p.getStockActual() <= 0).toList();
        List<ProductosEntity> bajos = productos.stream()
                .filter(p -> p.getStockActual() != null && p.getStockActual() > 0
                        && p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo())
                .toList();
        int unidades = productos.stream().map(ProductosEntity::getStockActual)
                .filter(stock -> stock != null).mapToInt(Integer::intValue).sum();
        List<MovimientosInventariosEntity> recientes = movimientosRepository.findRecientes(PageRequest.of(0, 10));
        return new Resumen(productos, agotados, bajos, unidades, recientes);
    }

    public record Resumen(List<ProductosEntity> productos, List<ProductosEntity> agotados,
                          List<ProductosEntity> bajos, int unidadesEnExistencia,
                          List<MovimientosInventariosEntity> movimientosRecientes) { }
}
