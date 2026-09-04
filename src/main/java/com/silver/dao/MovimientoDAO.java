package com.silver.dao;

import com.silver.model.Movimiento;
import com.silver.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    public void registrar(Connection con, Movimiento m) throws SQLException {
        String sql = "INSERT INTO movimientos (producto_id, tipo, cantidad, motivo, usuario_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getProductoId());
            ps.setString(2, m.getTipo());
            ps.setInt(3, m.getCantidad());
            ps.setString(4, m.getMotivo());
            ps.setInt(5, m.getUsuarioId());
            ps.executeUpdate();
        }
    }

    /** Version standalone (abre su propia conexion) para movimientos manuales desde el modulo de Inventario. */
    public void registrar(Movimiento m) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            registrar(con, m);
        }
    }

    public List<Movimiento> listarRecientes(int limite) throws SQLException {
        String sql = "SELECT m.id, m.producto_id, p.nombre AS producto_nombre, m.tipo, m.cantidad, " +
                     "m.motivo, m.usuario_id, u.nombre AS usuario_nombre, m.fecha " +
                     "FROM movimientos m " +
                     "JOIN productos p ON m.producto_id = p.id " +
                     "JOIN usuarios u ON m.usuario_id = u.id " +
                     "ORDER BY m.fecha DESC LIMIT ?";
        List<Movimiento> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Movimiento mapear(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setId(rs.getInt("id"));
        m.setProductoId(rs.getInt("producto_id"));
        m.setProductoNombre(rs.getString("producto_nombre"));
        m.setTipo(rs.getString("tipo"));
        m.setCantidad(rs.getInt("cantidad"));
        m.setMotivo(rs.getString("motivo"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setUsuarioNombre(rs.getString("usuario_nombre"));
        m.setFecha(rs.getTimestamp("fecha"));
        return m;
    }
}
