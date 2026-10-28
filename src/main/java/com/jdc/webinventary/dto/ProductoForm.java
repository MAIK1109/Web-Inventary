package com.jdc.webinventary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ProductoForm {
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 45, message = "El código no puede superar 45 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 45, message = "El nombre no puede superar 45 caracteres")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 45, message = "La categoría no puede superar 45 caracteres")
    private String categoriaProducto;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio de compra de referencia es obligatorio")
    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private Integer precioCompraReferencia;

    @NotNull(message = "El precio de venta es obligatorio")
    @PositiveOrZero(message = "El precio de venta no puede ser negativo")
    private Integer precioVenta;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @NotNull(message = "Selecciona el estado del producto")
    @Min(value = 0, message = "El estado no es válido")
    @Max(value = 1, message = "El estado no es válido")
    private Byte estado = 1;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoriaProducto() { return categoriaProducto; }
    public void setCategoriaProducto(String categoriaProducto) { this.categoriaProducto = categoriaProducto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getPrecioCompraReferencia() { return precioCompraReferencia; }
    public void setPrecioCompraReferencia(Integer precioCompraReferencia) { this.precioCompraReferencia = precioCompraReferencia; }
    public Integer getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Integer precioVenta) { this.precioVenta = precioVenta; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Byte getEstado() { return estado; }
    public void setEstado(Byte estado) { this.estado = estado; }
}
