package com.silver.controller;

import com.silver.dao.ProductoDAO;
import com.silver.model.Movimiento;
import com.silver.service.MovimientoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/movimientos")
public class MovimientoServlet extends HttpServlet {

    private final MovimientoService movimientoService = new MovimientoService();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("movimientos", movimientoService.listarRecientes(100));
            req.setAttribute("productos", productoDAO.buscar(null, null, null));
            req.setAttribute("bajoStock", productoDAO.listarBajoStock());
            req.getRequestDispatcher("/inventario.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar movimientos.");
            req.getRequestDispatcher("/inventario.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        int usuarioId = (int) session.getAttribute("usuarioId");

        Movimiento m = new Movimiento();
        m.setProductoId(Integer.parseInt(req.getParameter("productoId")));
        m.setTipo(req.getParameter("tipo"));
        m.setCantidad(Integer.parseInt(req.getParameter("cantidad")));
        m.setMotivo(req.getParameter("motivo"));
        m.setUsuarioId(usuarioId);

        MovimientoService.Resultado resultado = movimientoService.registrarMovimiento(m);

        req.getSession().setAttribute("mensajeMovimiento", resultado.mensaje);
        req.getSession().setAttribute("mensajeMovimientoTipo", resultado.exito ? "success" : "error");

        resp.sendRedirect(req.getContextPath() + "/movimientos");
    }
}
