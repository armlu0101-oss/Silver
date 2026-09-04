package com.silver.controller;

import com.silver.dao.ReporteDAO;
import com.silver.service.CreditoService;
import com.silver.service.MovimientoService;
import com.silver.dao.ProductoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoService movimientoService = new MovimientoService();
    private final CreditoService creditoService = new CreditoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("totalProductos", reporteDAO.totalProductosActivos());
            req.setAttribute("valorInventario", reporteDAO.valorInventario());
            req.setAttribute("ventasHoy", reporteDAO.ventasDelDia());
            req.setAttribute("bajoStockCount", reporteDAO.productosBajoStock());
            req.setAttribute("bajoStock", productoDAO.listarBajoStock());
            req.setAttribute("movimientosRecientes", movimientoService.listarRecientes(8));
            req.setAttribute("creditosPendientesTotal", creditoService.totalPendiente());
            req.setAttribute("creditosPendientesCount", creditoService.contarActivos());
            req.setAttribute("ventasSemana", reporteDAO.ventasUltimos7Dias());
            req.setAttribute("movimientosSemana", reporteDAO.movimientosUltimos7Dias());

            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al calcular los indicadores del dashboard.");
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        }
    }
}
