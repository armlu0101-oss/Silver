package com.silver.controller;

import com.silver.dao.ProveedorDAO;
import com.silver.model.Proveedor;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/proveedores")
public class ProveedorServlet extends HttpServlet {

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("proveedores", proveedorDAO.listarActivos());
            req.getRequestDispatcher("/proveedores.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar proveedores.");
            req.getRequestDispatcher("/proveedores.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("desactivar".equals(accion)) {
                proveedorDAO.desactivar(Integer.parseInt(req.getParameter("id")));
            } else {
                Proveedor p = new Proveedor();
                p.setNombre(req.getParameter("nombre"));
                p.setTelefono(req.getParameter("telefono"));
                p.setCorreo(req.getParameter("correo"));
                p.setDireccion(req.getParameter("direccion"));
                String idParam = req.getParameter("id");
                if (idParam != null && !idParam.isBlank()) {
                    p.setId(Integer.parseInt(idParam));
                    proveedorDAO.actualizar(p);
                } else {
                    proveedorDAO.guardar(p);
                }
            }
            resp.sendRedirect(req.getContextPath() + "/proveedores");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/proveedores");
        }
    }
}
