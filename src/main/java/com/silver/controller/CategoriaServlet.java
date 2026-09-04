package com.silver.controller;

import com.silver.dao.CategoriaDAO;
import com.silver.model.Categoria;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/categorias")
public class CategoriaServlet extends HttpServlet {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("categorias", categoriaDAO.listarActivas());
            req.getRequestDispatcher("/categorias.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar categorias.");
            req.getRequestDispatcher("/categorias.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("desactivar".equals(accion)) {
                categoriaDAO.desactivar(Integer.parseInt(req.getParameter("id")));
            } else {
                Categoria c = new Categoria();
                c.setNombre(req.getParameter("nombre"));
                String idParam = req.getParameter("id");
                if (idParam != null && !idParam.isBlank()) {
                    c.setId(Integer.parseInt(idParam));
                    categoriaDAO.actualizar(c);
                } else {
                    categoriaDAO.guardar(c);
                }
            }
            resp.sendRedirect(req.getContextPath() + "/categorias");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/categorias");
        }
    }
}
