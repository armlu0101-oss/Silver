package com.silver.service;

import com.silver.dao.MovimientoDAO;
import com.silver.dao.ProductoDAO;
import com.silver.model.Movimiento;
import com.silver.util.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MovimientoService {

    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    public static class Resultado {
        public final boolean exito;
        public final String mensaje;
        public Resultado(boolean exito, String mensaje) { this.exito = exito; this.mensaje = mensaje; }
    }

    /**
     * Registra un movimiento (ENTRADA, SALIDA, AJUSTE) y actualiza la existencia
     * del producto de forma transaccional: si algo falla, se revierte todo.
     */
    public Resultado registrarMovimiento(Movimiento m) {
        if (m.getCantidad() <= 0) {
            return new Resultado(false, "La cantidad debe ser mayor a cero.");
        }

        int delta = switch (m.getTipo()) {
            case "ENTRADA" -> m.getCantidad();
            case "SALIDA" -> -m.getCantidad();
            case "AJUSTE" -> m.getCantidad(); // puede ser positivo o negativo segun se capture
            default -> 0;
        };

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                if ("SALIDA".equals(m.getTipo())) {
                    var producto = productoDAO.buscarPorId(m.getProductoId());
                    if (producto == null || producto.getCantidad() < m.getCantidad()) {
                        con.rollback();
                        return new Resultado(false, "No hay suficiente existencia para esta salida.");
                    }
                }
                productoDAO.ajustarCantidad(con, m.getProductoId(), delta);
                movimientoDAO.registrar(con, m);
                con.commit();
                return new Resultado(true, "Movimiento registrado correctamente.");
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, "Error al registrar el movimiento.");
        }
    }

    public List<Movimiento> listarRecientes(int limite) throws SQLException {
        return movimientoDAO.listarRecientes(limite);
    }
}
