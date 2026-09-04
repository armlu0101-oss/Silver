package com.silver.service;

import com.silver.dao.UsuarioDAO;
import com.silver.model.Usuario;

import java.sql.SQLException;

/**
 * Capa de servicio: valida datos y aplica reglas de negocio antes
 * de llegar a la base de datos (control de roles, estatus, etc).
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public static class ResultadoLogin {
        public final boolean exito;
        public final String mensaje;
        public final Usuario usuario;

        public ResultadoLogin(boolean exito, String mensaje, Usuario usuario) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.usuario = usuario;
        }
    }

    public ResultadoLogin login(String usuario, String password) {
        if (usuario == null || usuario.isBlank() || password == null || password.isBlank()) {
            return new ResultadoLogin(false, "Usuario y contrasena son obligatorios.", null);
        }

        try {
            Usuario u = usuarioDAO.buscarPorUsuarioYPassword(usuario.trim(), password);

            if (u == null) {
                return new ResultadoLogin(false, "Usuario o contrasena incorrectos.", null);
            }

            if (!"ACTIVO".equalsIgnoreCase(u.getEstatus())) {
                return new ResultadoLogin(false, "El usuario se encuentra inactivo. Contacte al administrador.", null);
            }

            return new ResultadoLogin(true, "Bienvenido, " + u.getNombre(), u);

        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoLogin(false, "Error de conexion con la base de datos.", null);
        }
    }
}
