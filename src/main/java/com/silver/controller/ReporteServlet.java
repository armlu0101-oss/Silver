package com.silver.controller;

import com.silver.dao.ReporteDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/reportes")
public class ReporteServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Map<String, BigDecimal> ventas = reporteDAO.ventasUltimos7Dias();
            Map<String, int[]> movimientos = reporteDAO.movimientosUltimos7Dias();
            Map<String, Integer> masVendidos = reporteDAO.productosMasVendidos(8);

            req.setAttribute("ventasJson", mapaVentasAJson(ventas));
            req.setAttribute("movimientosJson", mapaMovimientosAJson(movimientos));
            req.setAttribute("masVendidosJson", mapaMasVendidosAJson(masVendidos));
            req.setAttribute("totalProductos", reporteDAO.totalProductosActivos());
            req.setAttribute("valorInventario", reporteDAO.valorInventario());

            req.getRequestDispatcher("/reportes.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al generar los reportes.");
            req.getRequestDispatcher("/reportes.jsp").forward(req, resp);
        }
    }

    private String mapaVentasAJson(Map<String, BigDecimal> datos) {
        StringBuilder labels = new StringBuilder("[");
        StringBuilder values = new StringBuilder("[");
        boolean first = true;
        for (var e : datos.entrySet()) {
            if (!first) { labels.append(","); values.append(","); }
            labels.append("\"").append(e.getKey()).append("\"");
            values.append(e.getValue());
            first = false;
        }
        labels.append("]");
        values.append("]");
        return "{\"labels\":" + labels + ",\"values\":" + values + "}";
    }

    private String mapaMovimientosAJson(Map<String, int[]> datos) {
        StringBuilder labels = new StringBuilder("[");
        StringBuilder entradas = new StringBuilder("[");
        StringBuilder salidas = new StringBuilder("[");
        boolean first = true;
        for (var e : datos.entrySet()) {
            if (!first) { labels.append(","); entradas.append(","); salidas.append(","); }
            labels.append("\"").append(e.getKey()).append("\"");
            entradas.append(e.getValue()[0]);
            salidas.append(e.getValue()[1]);
            first = false;
        }
        labels.append("]"); entradas.append("]"); salidas.append("]");
        return "{\"labels\":" + labels + ",\"entradas\":" + entradas + ",\"salidas\":" + salidas + "}";
    }

    private String mapaMasVendidosAJson(Map<String, Integer> datos) {
        StringBuilder labels = new StringBuilder("[");
        StringBuilder values = new StringBuilder("[");
        boolean first = true;
        for (var e : datos.entrySet()) {
            if (!first) { labels.append(","); values.append(","); }
            labels.append("\"").append(e.getKey().replace("\"", "'")).append("\"");
            values.append(e.getValue());
            first = false;
        }
        labels.append("]"); values.append("]");
        return "{\"labels\":" + labels + ",\"values\":" + values + "}";
    }
}
