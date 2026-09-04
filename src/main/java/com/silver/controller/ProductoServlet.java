package com.silver.controller;

import com.silver.dao.CategoriaDAO;
import com.silver.dao.ProveedorDAO;
import com.silver.model.Producto;
import com.silver.service.ProductoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet("/productos")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5 MB por imagen
public class ProductoServlet extends HttpServlet {

    private final ProductoService productoService = new ProductoService();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");

        try {
            if ("buscarJson".equals(accion)) {
                // Usado por el POS (Fase 3) para autocompletar productos
                responderJsonBusqueda(req, resp);
                return;
            }

            String texto = req.getParameter("q");
            String estadoStock = req.getParameter("estado");
            String catParam = req.getParameter("categoriaId");
            Integer categoriaId = (catParam != null && !catParam.isBlank()) ? Integer.parseInt(catParam) : null;

            List<Producto> productos = productoService.buscar(texto, categoriaId, estadoStock);

            req.setAttribute("productos", productos);
            req.setAttribute("categorias", categoriaDAO.listarActivas());
            req.setAttribute("proveedores", proveedorDAO.listarActivos());
            req.setAttribute("filtroTexto", texto);
            req.setAttribute("filtroCategoria", categoriaId);
            req.setAttribute("filtroEstado", estadoStock);

            req.getRequestDispatcher("/productos.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al consultar productos.");
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");

        try {
            if ("desactivar".equals(accion)) {
                int id = Integer.parseInt(req.getParameter("id"));
                productoService.desactivar(id);
                resp.sendRedirect(req.getContextPath() + "/productos");
                return;
            }

            Producto p = new Producto();
            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                p.setId(Integer.parseInt(idParam));
            }
            p.setNombre(req.getParameter("nombre"));
            p.setCodigo(req.getParameter("codigo"));
            p.setModelo(req.getParameter("modelo"));
            p.setMarca(req.getParameter("marca"));
            p.setTalla(req.getParameter("talla"));
            p.setColor(req.getParameter("color"));
            p.setPrecio(new BigDecimal(req.getParameter("precio")));
            p.setCantidad(Integer.parseInt(req.getParameter("cantidad")));
            p.setStockMinimo(Integer.parseInt(req.getParameter("stockMinimo")));

            String catId = req.getParameter("categoriaId");
            if (catId != null && !catId.isBlank()) p.setCategoriaId(Integer.parseInt(catId));

            String provId = req.getParameter("proveedorId");
            if (provId != null && !provId.isBlank()) p.setProveedorId(Integer.parseInt(provId));

            // Imagen: si suben una nueva se guarda en /assets/img/productos, si no se conserva la existente
            String imagenExistente = req.getParameter("imagenActual");
            p.setImagen(imagenExistente);

            Part parteImagen = req.getPart("imagen");
            if (parteImagen != null && parteImagen.getSize() > 0) {
                String nombreArchivo = guardarImagen(req, parteImagen);
                p.setImagen(nombreArchivo);
            }

            ProductoService.Resultado resultado = productoService.guardar(p);

            if (!resultado.exito) {
                req.setAttribute("error", resultado.mensaje);
                req.setAttribute("productos", productoService.buscar(null, null, null));
                req.setAttribute("categorias", categoriaDAO.listarActivas());
                req.setAttribute("proveedores", proveedorDAO.listarActivos());
                req.getRequestDispatcher("/productos.jsp").forward(req, resp);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/productos");

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al procesar el producto.");
            try {
                req.getRequestDispatcher("/productos.jsp").forward(req, resp);
            } catch (ServletException ignored) {}
        }
    }

    private String guardarImagen(HttpServletRequest req, Part parte) throws IOException {
        String extension = "";
        String original = parte.getSubmittedFileName();
        int punto = original != null ? original.lastIndexOf('.') : -1;
        if (punto >= 0) extension = original.substring(punto);

        String nombreArchivo = UUID.randomUUID() + extension;
        String directorioReal = req.getServletContext().getRealPath("/assets/img/productos");
        Files.createDirectories(Path.of(directorioReal));
        parte.write(directorioReal + "/" + nombreArchivo);
        return nombreArchivo;
    }

    private void responderJsonBusqueda(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String q = req.getParameter("q");
        List<Producto> productos = productoService.buscar(q, null, null);

        resp.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(p.getId()).append(",")
                .append("\"nombre\":\"").append(escapar(p.getNombre())).append("\",")
                .append("\"codigo\":\"").append(escapar(p.getCodigo())).append("\",")
                .append("\"talla\":\"").append(escapar(p.getTalla())).append("\",")
                .append("\"precio\":").append(p.getPrecio()).append(",")
                .append("\"cantidad\":").append(p.getCantidad())
                .append("}");
        }
        json.append("]");

        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }

    private String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
