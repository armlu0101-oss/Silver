package com.silver.service;

import com.silver.dao.LocatarioDAO;
import com.silver.model.Locatario;

import java.sql.SQLException;
import java.util.List;

public class LocatarioService {

    private final LocatarioDAO locatarioDAO = new LocatarioDAO();

    public static class Resultado {
        public final boolean exito;
        public final String mensaje;
        public Resultado(boolean exito, String mensaje) { this.exito = exito; this.mensaje = mensaje; }
    }

    public List<Locatario> listarActivos() throws SQLException {
        return locatarioDAO.listarActivos();
    }

    public Resultado guardar(Locatario l) {
        if (l.getNombre() == null || l.getNombre().isBlank()) {
            return new Resultado(false, "El nombre es obligatorio.");
        }
        try {
            if (l.getId() > 0) locatarioDAO.actualizar(l);
            else locatarioDAO.guardar(l);
            return new Resultado(true, "Locatario guardado.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, "Error al guardar el locatario.");
        }
    }

    public Resultado desactivar(int id) {
        try {
            locatarioDAO.desactivar(id);
            return new Resultado(true, "Locatario desactivado.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, "Error al desactivar.");
        }
    }
}
