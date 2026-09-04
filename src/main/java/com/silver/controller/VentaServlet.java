package com.silver.controller;

import com.silver.dao.ProductoDAO;
import com.silver.model.DetalleVenta;
import com.silver.model.Producto;
import com.silver.model.Venta;
import com.silver.service.VentaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del POS. El carrito se arma en el navegador (ventas.jsp) y se
 * envia como JSON simple: [{"productoId":1,"cantidad":2}, ...]
 * La validacion real de precio y existencia se hace en el servidor
 * consultando la base de datos, nunca confiando en lo enviado por el cliente.
 */
@WebServlet("/ventas")
public class VentaServlet extends HttpServlet {

    private final VentaService ventaService = new VentaService();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/ventas.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        int usuarioId = (int) session.getAttribute("usuarioId");
        String usuarioNombre = (String) session.getAttribute("usuarioNombre");

        try {
            String body = leerBody(req);
            List<int[]> items = parsearCarrito(body); // [productoId, cantidad]

            Venta venta = new Venta();
            venta.setUsuarioId(usuarioId);
            venta.setUsuarioNombre(usuarioNombre);

            List<DetalleVenta> detalles = new ArrayList<>();
            for (int[] item : items) {
                Producto p = productoDAO.buscarPorId(item[0]);
                if (p == null) continue;
                DetalleVenta d = new DetalleVenta();
                d.setProductoId(p.getId());
                d.setProductoNombre(p.getNombre());
                d.setCantidad(item[1]);
                d.setPrecioUnitario(p.getPrecio());
                detalles.add(d);
            }
            venta.setDetalles(detalles);

            VentaService.Resultado resultado = ventaService.procesarVenta(venta);

            try (PrintWriter out = resp.getWriter()) {
                if (resultado.exito) {
                    out.print("{\"exito\":true,\"ventaId\":" + resultado.ventaId + "}");
                } else {
                    out.print("{\"exito\":false,\"mensaje\":\"" + escapar(resultado.mensaje) + "\"}");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"exito\":false,\"mensaje\":\"Error al consultar productos.\"}");
            }
        }
    }

    private String leerBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    /** Parser minimo de JSON para el arreglo [{"productoId":1,"cantidad":2}] sin depender de librerias externas. */
    private List<int[]> parsearCarrito(String json) {
        List<int[]> items = new ArrayList<>();
        String[] objetos = json.replace("[", "").replace("]", "").split("\\},\\s*\\{");
        for (String obj : objetos) {
            String limpio = obj.replace("{", "").replace("}", "");
            Integer productoId = null, cantidad = null;
            for (String par : limpio.split(",")) {
                String[] kv = par.split(":");
                if (kv.length != 2) continue;
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                if (key.equals("productoId")) productoId = Integer.parseInt(value);
                if (key.equals("cantidad")) cantidad = Integer.parseInt(value);
            }
            if (productoId != null && cantidad != null) {
                items.add(new int[]{productoId, cantidad});
            }
        }
        return items;
    }

    private String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
