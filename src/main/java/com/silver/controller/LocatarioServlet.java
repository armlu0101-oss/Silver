package com.silver.controller;

import com.silver.model.Locatario;
import com.silver.service.LocatarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/locatarios")
public class LocatarioServlet extends HttpServlet {

    private final LocatarioService locatarioService = new LocatarioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("locatarios", locatarioService.listarActivos());
            req.getRequestDispatcher("/locatarios.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar locatarios.");
            req.getRequestDispatcher("/locatarios.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");

        if ("desactivar".equals(accion)) {
            locatarioService.desactivar(Integer.parseInt(req.getParameter("id")));
            resp.sendRedirect(req.getContextPath() + "/locatarios");
            return;
        }

        Locatario l = new Locatario();
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) l.setId(Integer.parseInt(idParam));
        l.setNombre(req.getParameter("nombre"));
        l.setTelefono(req.getParameter("telefono"));
        l.setDireccion(req.getParameter("direccion"));

        locatarioService.guardar(l);
        resp.sendRedirect(req.getContextPath() + "/locatarios");
    }
}
