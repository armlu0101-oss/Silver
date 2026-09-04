package com.silver.service;

import com.silver.dao.ProductoDAO;
import com.silver.model.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO = new ProductoDAO();

    public static class Resultado {
        public final boolean exito;
        public final String mensaje;
        public Resultado(boolean exito, String mensaje) { this.exito = exito; this.mensaje = mensaje; }
    }

    public List<Producto> buscar(String texto, Integer categoriaId, String estadoStock) throws SQLException {
        return productoDAO.buscar(texto, categoriaId, estadoStock);
    }

    public List<Producto> listarBajoStock() throws SQLException {
        return productoDAO.listarBajoStock();
    }

    public Producto obtener(int id) throws SQLException {
        return productoDAO.buscarPorId(id);
    }

    public Resultado guardar(Producto p) {
        Resultado validacion = validar(p);
        if (!validacion.exito) return validacion;

        try {
            if (p.getId() > 0) {
                productoDAO.actualizar(p);
            } else {
                if (productoDAO.existeCodigo(p.getCodigo())) {
                    return new Resultado(false, "Ya existe un producto con ese codigo.");
                }
                productoDAO.guardar(p);
            }
            return new Resultado(true, "Producto guardado correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, "Error al guardar el producto en la base de datos.");
        }
    }

    public Resultado desactivar(int id) {
        try {
            productoDAO.desactivar(id);
            return new Resultado(true, "Producto desactivado.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, "Error al desactivar el producto.");
        }
    }

    private Resultado validar(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank())
            return new Resultado(false, "El nombre es obligatorio.");
        if (p.getCodigo() == null || p.getCodigo().isBlank())
            return new Resultado(false, "El codigo es obligatorio.");
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) < 0)
            return new Resultado(false, "El precio debe ser mayor o igual a cero.");
        if (p.getCantidad() < 0)
            return new Resultado(false, "La cantidad no puede ser negativa.");
        if (p.getStockMinimo() < 0)
            return new Resultado(false, "El stock minimo no puede ser negativo.");
        return new Resultado(true, "OK");
    }
}
