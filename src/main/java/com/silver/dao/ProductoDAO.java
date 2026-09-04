package com.silver.dao;

import com.silver.model.Producto;
import com.silver.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SELECT_BASE =
        "SELECT p.id, p.nombre, p.codigo, p.categoria_id, c.nombre AS categoria_nombre, " +
        "p.proveedor_id, pr.nombre AS proveedor_nombre, p.modelo, p.marca, p.talla, p.color, " +
        "p.precio, p.cantidad, p.stock_minimo, p.imagen, p.estatus " +
        "FROM productos p " +
        "LEFT JOIN categorias c ON p.categoria_id = c.id " +
        "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id ";

    /** Busqueda con filtros opcionales: texto libre (nombre/codigo/modelo/marca), categoria y estado de stock. */
    public List<Producto> buscar(String texto, Integer categoriaId, String estadoStock) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE p.estatus = 'ACTIVO' ");
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.codigo LIKE ? OR p.modelo LIKE ? OR p.marca LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (categoriaId != null) {
            sql.append("AND p.categoria_id = ? ");
            params.add(categoriaId);
        }
        if (estadoStock != null) {
            switch (estadoStock) {
                case "BAJO" -> sql.append("AND p.cantidad > 0 AND p.cantidad <= p.stock_minimo ");
                case "AGOTADO" -> sql.append("AND p.cantidad <= 0 ");
                default -> {}
            }
        }
        sql.append("ORDER BY p.nombre");

        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = "SELECT 1 FROM productos WHERE codigo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int guardar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (nombre, codigo, categoria_id, proveedor_id, modelo, marca, " +
                     "talla, color, precio, cantidad, stock_minimo, imagen, estatus) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'ACTIVO')";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre=?, codigo=?, categoria_id=?, proveedor_id=?, modelo=?, " +
                     "marca=?, talla=?, color=?, precio=?, cantidad=?, stock_minimo=?, imagen=? WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignarParametros(ps, p);
            ps.setInt(13, p.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE productos SET estatus = 'INACTIVO' WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Ajusta existencia sumando (positivo) o restando (negativo) dentro de una conexion/transaccion dada. */
    public void ajustarCantidad(Connection con, int productoId, int delta) throws SQLException {
        String sql = "UPDATE productos SET cantidad = cantidad + ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    public List<Producto> listarBajoStock() throws SQLException {
        String sql = SELECT_BASE + "WHERE p.estatus = 'ACTIVO' AND p.cantidad <= p.stock_minimo ORDER BY p.cantidad ASC";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private void asignarParametros(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getNombre());
        ps.setString(2, p.getCodigo());
        if (p.getCategoriaId() != null) ps.setInt(3, p.getCategoriaId()); else ps.setNull(3, Types.INTEGER);
        if (p.getProveedorId() != null) ps.setInt(4, p.getProveedorId()); else ps.setNull(4, Types.INTEGER);
        ps.setString(5, p.getModelo());
        ps.setString(6, p.getMarca());
        ps.setString(7, p.getTalla());
        ps.setString(8, p.getColor());
        ps.setBigDecimal(9, p.getPrecio());
        ps.setInt(10, p.getCantidad());
        ps.setInt(11, p.getStockMinimo());
        ps.setString(12, p.getImagen());
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCodigo(rs.getString("codigo"));
        int catId = rs.getInt("categoria_id");
        p.setCategoriaId(rs.wasNull() ? null : catId);
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        int provId = rs.getInt("proveedor_id");
        p.setProveedorId(rs.wasNull() ? null : provId);
        p.setProveedorNombre(rs.getString("proveedor_nombre"));
        p.setModelo(rs.getString("modelo"));
        p.setMarca(rs.getString("marca"));
        p.setTalla(rs.getString("talla"));
        p.setColor(rs.getString("color"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setCantidad(rs.getInt("cantidad"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setImagen(rs.getString("imagen"));
        p.setEstatus(rs.getString("estatus"));
        return p;
    }
}
