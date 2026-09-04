package com.silver.dao;

import com.silver.model.DetalleVenta;
import com.silver.model.Movimiento;
import com.silver.model.Venta;
import com.silver.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;

public class VentaDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    /**
     * Registra la venta completa de forma transaccional:
     * 1) inserta la venta, 2) inserta cada detalle, 3) descuenta inventario,
     * 4) registra el movimiento de salida correspondiente.
     * Si cualquier paso falla, se revierte todo (rollback).
     */
    public int registrarVenta(Venta venta) throws SQLException {
        String sqlVenta = "INSERT INTO ventas (usuario_id, total) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int ventaId;
                try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, venta.getUsuarioId());
                    ps.setBigDecimal(2, venta.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        ventaId = rs.getInt(1);
                    }
                }

                for (DetalleVenta d : venta.getDetalles()) {
                    // Verificar existencia suficiente antes de descontar
                    var producto = productoDAO.buscarPorId(d.getProductoId());
                    if (producto == null || producto.getCantidad() < d.getCantidad()) {
                        con.rollback();
                        throw new SQLException("Existencia insuficiente para: " +
                                (producto != null ? producto.getNombre() : "producto #" + d.getProductoId()));
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                        ps.setInt(1, ventaId);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.executeUpdate();
                    }

                    productoDAO.ajustarCantidad(con, d.getProductoId(), -d.getCantidad());

                    Movimiento mov = new Movimiento();
                    mov.setProductoId(d.getProductoId());
                    mov.setTipo("SALIDA");
                    mov.setCantidad(d.getCantidad());
                    mov.setMotivo("Venta POS #" + ventaId);
                    mov.setUsuarioId(venta.getUsuarioId());
                    movimientoDAO.registrar(con, mov);
                }

                con.commit();
                return ventaId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Venta obtenerVentaCompleta(int ventaId) throws SQLException {
        String sqlVenta = "SELECT v.id, v.fecha, v.usuario_id, u.nombre AS usuario_nombre, v.total " +
                          "FROM ventas v JOIN usuarios u ON v.usuario_id = u.id WHERE v.id = ?";
        String sqlDetalle = "SELECT d.id, d.producto_id, p.nombre AS producto_nombre, d.cantidad, d.precio_unitario, d.subtotal " +
                            "FROM detalle_venta d JOIN productos p ON d.producto_id = p.id WHERE d.venta_id = ?";

        Venta venta = null;
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sqlVenta)) {
                ps.setInt(1, ventaId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        venta = new Venta();
                        venta.setId(rs.getInt("id"));
                        venta.setFecha(rs.getTimestamp("fecha"));
                        venta.setUsuarioId(rs.getInt("usuario_id"));
                        venta.setUsuarioNombre(rs.getString("usuario_nombre"));
                        venta.setTotal(rs.getBigDecimal("total"));
                    }
                }
            }
            if (venta == null) return null;

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, ventaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleVenta d = new DetalleVenta();
                        d.setId(rs.getInt("id"));
                        d.setVentaId(ventaId);
                        d.setProductoId(rs.getInt("producto_id"));
                        d.setProductoNombre(rs.getString("producto_nombre"));
                        d.setCantidad(rs.getInt("cantidad"));
                        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                        d.setSubtotal(rs.getBigDecimal("subtotal"));
                        venta.getDetalles().add(d);
                    }
                }
            }
        }
        return venta;
    }
}
