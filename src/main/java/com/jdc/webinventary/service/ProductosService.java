package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.ProductoForm;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.repository.ProductosRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ProductosService {
    private final ProductosRepository productos;

    public ProductosService(ProductosRepository productos) {
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<ProductosEntity> listar() {
        return productos.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    @Transactional(readOnly = true)
    public ProductosEntity obtener(Integer id) {
        return productos.findById(id).orElseThrow(ProductoNoEncontradoException::new);
    }

    @Transactional(readOnly = true)
    public boolean codigoRegistrado(String codigo, Integer idIgnorado) {
        String normalizado = normalizarCodigo(codigo);
        return idIgnorado == null
                ? productos.existsByCodigoIgnoreCase(normalizado)
                : productos.existsByCodigoIgnoreCaseAndIdProductoNot(normalizado, idIgnorado);
    }

    @Transactional
    public void crear(ProductoForm form) {
        ProductosEntity producto = new ProductosEntity();
        actualizarDatos(producto, form);
        producto.setStockActual(0);
        guardar(producto);
    }

    @Transactional
    public void actualizar(Integer id, ProductoForm form) {
        ProductosEntity producto = obtener(id);
        actualizarDatos(producto, form);
        guardar(producto);
    }

    private void actualizarDatos(ProductosEntity producto, ProductoForm form) {
        producto.setCodigo(normalizarCodigo(form.getCodigo()));
        producto.setNombre(form.getNombre().trim());
        producto.setCategoriaProducto(form.getCategoriaProducto().trim());
        producto.setDescripcion(form.getDescripcion() == null || form.getDescripcion().isBlank()
                ? null : form.getDescripcion().trim());
        producto.setPrecioCompraReferencia(form.getPrecioCompraReferencia());
        producto.setPrecioVenta(form.getPrecioVenta());
        producto.setStockMinimo(form.getStockMinimo());
        producto.setEstado(form.getEstado());
    }

    private void guardar(ProductosEntity producto) {
        try {
            productos.saveAndFlush(producto);
        } catch (DataIntegrityViolationException ex) {
            throw new CodigoDuplicadoException();
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    public static class CodigoDuplicadoException extends RuntimeException {
        public CodigoDuplicadoException() { super("Ya existe un producto con ese código."); }
    }

    public static class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException() { super("No se encontró el producto solicitado."); }
    }
}
