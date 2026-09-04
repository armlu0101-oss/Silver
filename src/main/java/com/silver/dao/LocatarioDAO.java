package com.silver.dao;

import com.silver.model.Locatario;
import com.silver.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocatarioDAO {

    public List<Locatario> listarActivos() throws SQLException {
        String sql = "SELECT id, nombre, telefono, direccion, estatus FROM locatarios WHERE estatus = 'ACTIVO' ORDER BY nombre";
        List<Locatario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Locatario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, telefono, direccion, estatus FROM locatarios WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void guardar(Locatario l) throws SQLException {
        String sql = "INSERT INTO locatarios (nombre, telefono, direccion, estatus) VALUES (?,?,?,'ACTIVO')";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getNombre());
            ps.setString(2, l.getTelefono());
            ps.setString(3, l.getDireccion());
            ps.executeUpdate();
        }
    }

    public void actualizar(Locatario l) throws SQLException {
        String sql = "UPDATE locatarios SET nombre=?, telefono=?, direccion=? WHERE id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getNombre());
            ps.setString(2, l.getTelefono());
            ps.setString(3, l.getDireccion());
            ps.setInt(4, l.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE locatarios SET estatus = 'INACTIVO' WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Locatario mapear(ResultSet rs) throws SQLException {
        Locatario l = new Locatario();
        l.setId(rs.getInt("id"));
        l.setNombre(rs.getString("nombre"));
        l.setTelefono(rs.getString("telefono"));
        l.setDireccion(rs.getString("direccion"));
        l.setEstatus(rs.getString("estatus"));
        return l;
    }
}
