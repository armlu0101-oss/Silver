package com.silver.controller;

import com.silver.dao.LocatarioDAO;
import com.silver.dao.ProductoDAO;
import com.silver.model.Credito;
import com.silver.model.DetalleCredito;
import com.silver.model.Producto;
import com.silver.service.CreditoService;

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

@WebServlet("/creditos")
public class CreditoServlet extends HttpServlet {


    @Override
    public void init() throws ServletException {
        System.out.println("===== CREDITO SERVLET CARGADO =====");
    }


    private final CreditoService creditoService = new CreditoService();
    private final LocatarioDAO locatarioDAO = new LocatarioDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        try {
            if ("detalleJson".equals(accion)) {
                responderDetalleJson(req, resp);
                return;
            }

            req.setAttribute("creditos", creditoService.listarActivos());
            req.setAttribute("locatarios", locatarioDAO.listarActivos());
            req.setAttribute("productos", productoDAO.buscar(null, null, null));
            req.setAttribute("totalPendiente", creditoService.totalPendiente());
            req.getRequestDispatcher("/creditos.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar creditos.");
            req.getRequestDispatcher("/creditos.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accion = req.getParameter("accion");
        HttpSession session = req.getSession(false);
        int usuarioId = (int) session.getAttribute("usuarioId");

        resp.setContentType("application/json;charset=UTF-8");

        if ("abonar".equals(accion)) {
            int creditoId = Integer.parseInt(req.getParameter("creditoId"));
            BigDecimal monto = new BigDecimal(req.getParameter("monto"));
            CreditoService.Resultado resultado = creditoService.registrarAbono(creditoId, monto);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"exito\":" + resultado.exito + ",\"mensaje\":\"" + escapar(resultado.mensaje) + "\"}");
            }
            return;
        }

        // accion == "entregar": el body es JSON del carrito + locatarioId
        try {
            String body = leerBody(req);
            int locatarioId = Integer.parseInt(extraerValor(body, "locatarioId"));
            List<int[]> items = parsearItems(body);

            Credito credito = new Credito();
            credito.setLocatarioId(locatarioId);

            List<DetalleCredito> detalles = new ArrayList<>();
            for (int[] item : items) {
                Producto p = productoDAO.buscarPorId(item[0]);
                if (p == null) {
                    continue;
                }
                DetalleCredito d = new DetalleCredito();
                d.setProductoId(p.getId());
                d.setProductoNombre(p.getNombre());
                d.setCantidad(item[1]);
                d.setPrecioUnitario(p.getPrecio());
                detalles.add(d);
            }
            credito.setDetalles(detalles);

            CreditoService.Resultado resultado = creditoService.entregarMercancia(credito, usuarioId);
            try (PrintWriter out = resp.getWriter()) {
                if (resultado.exito) {
                    out.print("{\"exito\":true,\"creditoId\":" + resultado.creditoId + "}");
                } else {
                    out.print("{\"exito\":false,\"mensaje\":\"" + escapar(resultado.mensaje) + "\"}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().print("{\"exito\":false,\"mensaje\":\"Error al procesar el credito.\"}");
        }
    }

    private void responderDetalleJson(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {

            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(
                    "{\"exito\":false,\"mensaje\":\"ID de credito vacío\"}"
            );
            return;
        }

        int id = Integer.parseInt(idParam);
        Credito c = creditoService.obtenerCompleto(id);
        resp.setContentType("application/json;charset=UTF-8");

        if (c == null) {
            resp.getWriter().print("{\"exito\":false}");
            return;
        }

        StringBuilder detallesJson = new StringBuilder("[");
        for (int i = 0; i < c.getDetalles().size(); i++) {
            var d = c.getDetalles().get(i);
            if (i > 0) {
                detallesJson.append(",");
            }
            detallesJson.append("{\"nombre\":\"").append(escapar(d.getProductoNombre())).append("\",")
                    .append("\"cantidad\":").append(d.getCantidad()).append(",")
                    .append("\"precio\":").append(d.getPrecioUnitario()).append("}");
        }
        detallesJson.append("]");

        StringBuilder abonosJson = new StringBuilder("[");
        for (int i = 0; i < c.getAbonos().size(); i++) {
            var a = c.getAbonos().get(i);
            if (i > 0) {
                abonosJson.append(",");
            }
            abonosJson.append("{\"monto\":").append(a.getMonto()).append(",")
                    .append("\"fecha\":\"").append(a.getFecha()).append("\"}");
        }
        abonosJson.append("]");

        String json = "{\"exito\":true,\"id\":" + c.getId()
                + ",\"locatario\":\"" + escapar(c.getLocatarioNombre()) + "\""
                + ",\"total\":" + c.getTotal()
                + ",\"saldo\":" + c.getSaldo()
                + ",\"estado\":\"" + c.getEstado() + "\""
                + ",\"detalles\":" + detallesJson
                + ",\"abonos\":" + abonosJson + "}";

        resp.getWriter().print(json);
    }

    private String leerBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String extraerValor(String json, String clave) {
        int idx = json.indexOf("\"" + clave + "\"");
        if (idx < 0) {
            return "0";
        }
        int inicio = json.indexOf(':', idx) + 1;
        int fin = inicio;
        while (fin < json.length() && (Character.isDigit(json.charAt(fin)))) {
            fin++;
        }
        return json.substring(inicio, fin).trim();
    }

    /**
     * Extrae el arreglo "items":[{"productoId":1,"cantidad":2}] del JSON
     * recibido.
     */
    private List<int[]> parsearItems(String json) {
        List<int[]> items = new ArrayList<>();
        int idxItems = json.indexOf("\"items\"");
        if (idxItems < 0) {
            return items;
        }
        int inicioArr = json.indexOf('[', idxItems);
        int finArr = json.indexOf(']', inicioArr);
        String arr = json.substring(inicioArr + 1, finArr);
        if (arr.isBlank()) {
            return items;
        }

        String[] objetos = arr.split("\\},\\s*\\{");
        for (String obj : objetos) {
            String limpio = obj.replace("{", "").replace("}", "");
            Integer productoId = null, cantidad = null;
            for (String par : limpio.split(",")) {
                String[] kv = par.split(":");
                if (kv.length != 2) {
                    continue;
                }
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                if (key.equals("productoId")) {
                    productoId = Integer.parseInt(value);
                }
                if (key.equals("cantidad")) {
                    cantidad = Integer.parseInt(value);
                }
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
