package com.silver.dao;

import com.silver.model.Usuario;
import com.silver.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO de usuarios. Todas las consultas usan PreparedStatement para
 * evitar inyeccion SQL, tal como exige el modulo de seguridad.
 */
public class UsuarioDAO {

    public Usuario buscarPorUsuarioYPassword(String usuario, String password) throws SQLException {
        String sql = "SELECT id, nombre, usuario, password, rol, estatus " +
                     "FROM usuarios WHERE usuario = ? AND password = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        String sql = "SELECT id, nombre, usuario, password, rol, estatus " +
                     "FROM usuarios WHERE usuario = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("usuario"),
                rs.getString("password"),
                rs.getString("rol"),
                rs.getString("estatus")
        );
    }
}
