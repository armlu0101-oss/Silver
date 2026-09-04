package com.silver.controller;

import com.silver.model.Venta;
import com.silver.service.TicketService;
import com.silver.service.VentaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Maneja tanto la impresion inicial del ticket (justo despues de la venta)
 * como la reimpresion posterior indicando el numero de venta.
 */
@WebServlet("/ticket")
public class TicketServlet extends HttpServlet {

    private final VentaService ventaService = new VentaService();
    private final TicketService ticketService = new TicketService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        int ventaId = Integer.parseInt(req.getParameter("ventaId"));
        int ancho = "58".equals(req.getParameter("papel")) ? 32 : 48;
        String impresora = req.getParameter("impresora"); // null = predeterminada del sistema

        try (PrintWriter out = resp.getWriter()) {
            Venta venta = ventaService.obtenerVenta(ventaId);
            if (venta == null) {
                out.print("{\"exito\":false,\"mensaje\":\"Venta no encontrada.\"}");
                return;
            }

            byte[] ticket = ticketService.generarTicket(venta, ancho);

            try {
                ticketService.imprimir(ticket, impresora);
                out.print("{\"exito\":true,\"mensaje\":\"Ticket enviado a la impresora.\"}");
            } catch (Exception ePrint) {
                // Si no hay impresora fisica conectada (ej. entorno de pruebas),
                // se informa sin interrumpir el flujo de la venta.
                out.print("{\"exito\":false,\"mensaje\":\"" + escapar(ePrint.getMessage()) + "\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().print("{\"exito\":false,\"mensaje\":\"Error al consultar la venta.\"}");
        }
    }

    private String escapar(String s) {
        return s == null ? "Error desconocido al imprimir." : s.replace("\"", "\\\"");
    }
}
