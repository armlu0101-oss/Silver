package com.silver.dao;

import com.silver.model.Proveedor;
import com.silver.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listarActivos() throws SQLException {
        String sql = "SELECT id, nombre, telefono, correo, direccion, estatus " +
                     "FROM proveedores WHERE estatus = 'ACTIVO' ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void guardar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedores (nombre, telefono, correo, direccion, estatus) VALUES (?,?,?,?,'ACTIVO')";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTelefono());
            ps.setString(3, p.getCorreo());
            ps.setString(4, p.getDireccion());
            ps.executeUpdate();
        }
    }

    public void actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedores SET nombre=?, telefono=?, correo=?, direccion=? WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getTelefono());
            ps.setString(3, p.getCorreo());
            ps.setString(4, p.getDireccion());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE proveedores SET estatus = 'INACTIVO' WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("telefono"),
                rs.getString("correo"), rs.getString("direccion"), rs.getString("estatus"));
    }
}
