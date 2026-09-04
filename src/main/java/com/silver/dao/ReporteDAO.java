package com.silver.dao;

import com.silver.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consultas de agregacion para el Dashboard y el modulo de Reportes.
 * Todas usan PreparedStatement y se limitan a lectura (SELECT).
 */
public class ReporteDAO {

    public int totalProductosActivos() throws SQLException {
        return escalarInt("SELECT COUNT(*) FROM productos WHERE estatus = 'ACTIVO'");
    }

    public BigDecimal valorInventario() throws SQLException {
        return escalarDecimal("SELECT COALESCE(SUM(precio * cantidad),0) FROM productos WHERE estatus = 'ACTIVO'");
    }

    public BigDecimal ventasDelDia() throws SQLException {
        return escalarDecimal("SELECT COALESCE(SUM(total),0) FROM ventas WHERE DATE(fecha) = CURDATE()");
    }

    public int productosBajoStock() throws SQLException {
        return escalarInt("SELECT COUNT(*) FROM productos WHERE estatus='ACTIVO' AND cantidad <= stock_minimo");
    }

    /** Ventas totales por dia de los ultimos 7 dias (incluye dias en cero). */
    public Map<String, BigDecimal> ventasUltimos7Dias() throws SQLException {
        String sql = "SELECT DATE(fecha) AS dia, SUM(total) AS total FROM ventas " +
                     "WHERE fecha >= CURDATE() - INTERVAL 6 DAY GROUP BY DATE(fecha)";
        Map<String, BigDecimal> porDia = new LinkedHashMap<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                porDia.put(rs.getDate("dia").toString(), rs.getBigDecimal("total"));
            }
        }
        return completarUltimos7Dias(porDia, BigDecimal.ZERO);
    }

    /** Entradas vs salidas de inventario por dia, ultimos 7 dias. */
    public Map<String, int[]> movimientosUltimos7Dias() throws SQLException {
        String sql = "SELECT DATE(fecha) AS dia, tipo, SUM(cantidad) AS total FROM movimientos " +
                     "WHERE fecha >= CURDATE() - INTERVAL 6 DAY AND tipo IN ('ENTRADA','SALIDA') " +
                     "GROUP BY DATE(fecha), tipo";
        Map<String, int[]> resultado = new LinkedHashMap<>(); // [entradas, salidas]
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dia = rs.getDate("dia").toString();
                int[] valores = resultado.computeIfAbsent(dia, k -> new int[]{0, 0});
                if ("ENTRADA".equals(rs.getString("tipo"))) valores[0] = rs.getInt("total");
                else valores[1] = rs.getInt("total");
            }
        }
        // Completar dias faltantes
        for (int i = 6; i >= 0; i--) {
            String dia = java.time.LocalDate.now().minusDays(i).toString();
            resultado.putIfAbsent(dia, new int[]{0, 0});
        }
        return resultado;
    }

    /** Top productos mas vendidos (por cantidad), ultimos 30 dias. */
    public Map<String, Integer> productosMasVendidos(int limite) throws SQLException {
        String sql = "SELECT p.nombre, SUM(d.cantidad) AS total FROM detalle_venta d " +
                     "JOIN productos p ON d.producto_id = p.id " +
                     "JOIN ventas v ON d.venta_id = v.id " +
                     "WHERE v.fecha >= CURDATE() - INTERVAL 30 DAY " +
                     "GROUP BY p.id, p.nombre ORDER BY total DESC LIMIT ?";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.put(rs.getString("nombre"), rs.getInt("total"));
            }
        }
        return resultado;
    }

    private Map<String, BigDecimal> completarUltimos7Dias(Map<String, BigDecimal> datos, BigDecimal porDefecto) {
        Map<String, BigDecimal> completo = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String dia = java.time.LocalDate.now().minusDays(i).toString();
            completo.put(dia, datos.getOrDefault(dia, porDefecto));
        }
        return completo;
    }

    private int escalarInt(String sql) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private BigDecimal escalarDecimal(String sql) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}
