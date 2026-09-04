package com.silver.dao;

import com.silver.model.Categoria;
import com.silver.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarActivas() throws SQLException {
        String sql = "SELECT id, nombre, estatus FROM categorias WHERE estatus = 'ACTIVO' ORDER BY nombre";
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre"), rs.getString("estatus")));
            }
        }
        return lista;
    }

    public void guardar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, estatus) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, "ACTIVO");
            ps.executeUpdate();
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE categorias SET estatus = 'INACTIVO' WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
