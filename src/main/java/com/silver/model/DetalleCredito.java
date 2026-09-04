package com.silver.model;

import java.math.BigDecimal;

public class DetalleCredito {
    private int id;
    private int creditoId;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal precioUnitario;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreditoId() { return creditoId; }
    public void setCreditoId(int creditoId) { this.creditoId = creditoId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
