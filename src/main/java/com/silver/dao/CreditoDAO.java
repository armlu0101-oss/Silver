package com.silver.dao;

import com.silver.model.Abono;
import com.silver.model.Credito;
import com.silver.model.DetalleCredito;
import com.silver.model.Movimiento;
import com.silver.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreditoDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    /**
     * Entrega mercancia a credito a un locatario: crea el credito, su detalle,
     * descuenta el inventario y registra el movimiento de salida. Todo en una
     * sola transaccion (si algo falla, se revierte por completo).
     */
    public int registrarCredito(Credito credito, int usuarioId) throws SQLException {
        String sqlCredito = "INSERT INTO creditos (locatario_id, total, saldo, estado) VALUES (?,?,?,'PENDIENTE')";
        String sqlDetalle = "INSERT INTO detalle_credito (credito_id, producto_id, cantidad, precio_unitario) VALUES (?,?,?,?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int creditoId;
                try (PreparedStatement ps = con.prepareStatement(sqlCredito, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, credito.getLocatarioId());
                    ps.setBigDecimal(2, credito.getTotal());
                    ps.setBigDecimal(3, credito.getTotal());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        creditoId = rs.getInt(1);
                    }
                }

                for (DetalleCredito d : credito.getDetalles()) {
                    var producto = productoDAO.buscarPorId(d.getProductoId());
                    if (producto == null || producto.getCantidad() < d.getCantidad()) {
                        con.rollback();
                        throw new SQLException("Existencia insuficiente para: " +
                                (producto != null ? producto.getNombre() : "producto #" + d.getProductoId()));
                    }

                    try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                        ps.setInt(1, creditoId);
                        ps.setInt(2, d.getProductoId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.executeUpdate();
                    }

                    productoDAO.ajustarCantidad(con, d.getProductoId(), -d.getCantidad());

                    Movimiento mov = new Movimiento();
                    mov.setProductoId(d.getProductoId());
                    mov.setTipo("SALIDA");
                    mov.setCantidad(d.getCantidad());
                    mov.setMotivo("Credito a locatario #" + credito.getLocatarioId() + " (credito #" + creditoId + ")");
                    mov.setUsuarioId(usuarioId);
                    movimientoDAO.registrar(con, mov);
                }

                con.commit();
                return creditoId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Registra un abono (pago) contra un credito, ajustando saldo y estado. */
    public void registrarAbono(int creditoId, BigDecimal monto) throws SQLException {
        String sqlSaldo = "SELECT saldo FROM creditos WHERE id = ? FOR UPDATE";
        String sqlAbono = "INSERT INTO abonos (credito_id, monto) VALUES (?, ?)";
        String sqlUpdate = "UPDATE creditos SET saldo = ?, estado = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                BigDecimal saldoActual;
                try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                    ps.setInt(1, creditoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Credito no encontrado.");
                        saldoActual = rs.getBigDecimal("saldo");
                    }
                }

                if (monto.compareTo(saldoActual) > 0) {
                    con.rollback();
                    throw new SQLException("El abono no puede ser mayor al saldo pendiente ($" + saldoActual + ").");
                }

                BigDecimal nuevoSaldo = saldoActual.subtract(monto);
                String nuevoEstado = nuevoSaldo.compareTo(BigDecimal.ZERO) == 0 ? "PAGADO" : "PENDIENTE";

                try (PreparedStatement ps = con.prepareStatement(sqlAbono)) {
                    ps.setInt(1, creditoId);
                    ps.setBigDecimal(2, monto);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setBigDecimal(1, nuevoSaldo);
                    ps.setString(2, nuevoEstado);
                    ps.setInt(3, creditoId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<Credito> listarActivos() throws SQLException {
        String sql = "SELECT c.id, c.fecha, c.locatario_id, l.nombre AS locatario_nombre, c.total, c.saldo, c.estado " +
                     "FROM creditos c JOIN locatarios l ON c.locatario_id = l.id " +
                     "WHERE c.estado <> 'PAGADO' ORDER BY c.fecha DESC";
        List<Credito> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Credito obtenerCompleto(int id) throws SQLException {
        String sqlCredito = "SELECT c.id, c.fecha, c.locatario_id, l.nombre AS locatario_nombre, c.total, c.saldo, c.estado " +
                            "FROM creditos c JOIN locatarios l ON c.locatario_id = l.id WHERE c.id = ?";
        String sqlDetalle = "SELECT d.id, d.producto_id, p.nombre AS producto_nombre, d.cantidad, d.precio_unitario " +
                            "FROM detalle_credito d JOIN productos p ON d.producto_id = p.id WHERE d.credito_id = ?";
        String sqlAbonos = "SELECT id, credito_id, monto, fecha FROM abonos WHERE credito_id = ? ORDER BY fecha";

        Credito credito = null;
        try (Connection con = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sqlCredito)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) credito = mapear(rs);
                }
            }
            if (credito == null) return null;

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleCredito d = new DetalleCredito();
                        d.setId(rs.getInt("id"));
                        d.setCreditoId(id);
                        d.setProductoId(rs.getInt("producto_id"));
                        d.setProductoNombre(rs.getString("producto_nombre"));
                        d.setCantidad(rs.getInt("cantidad"));
                        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                        credito.getDetalles().add(d);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlAbonos)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Abono a = new Abono();
                        a.setId(rs.getInt("id"));
                        a.setCreditoId(id);
                        a.setMonto(rs.getBigDecimal("monto"));
                        a.setFecha(rs.getTimestamp("fecha"));
                        credito.getAbonos().add(a);
                    }
                }
            }
        }
        return credito;
    }

    public BigDecimal totalPendiente() throws SQLException {
        String sql = "SELECT COALESCE(SUM(saldo),0) AS total FROM creditos WHERE estado <> 'PAGADO'";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal("total");
        }
    }

    public int contarActivos() throws SQLException {
        String sql = "SELECT COUNT(*) AS n FROM creditos WHERE estado <> 'PAGADO'";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt("n");
        }
    }

    private Credito mapear(ResultSet rs) throws SQLException {
        Credito c = new Credito();
        c.setId(rs.getInt("id"));
        c.setFecha(rs.getTimestamp("fecha"));
        c.setLocatarioId(rs.getInt("locatario_id"));
        c.setLocatarioNombre(rs.getString("locatario_nombre"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setSaldo(rs.getBigDecimal("saldo"));
        c.setEstado(rs.getString("estado"));
        return c;
    }
}
