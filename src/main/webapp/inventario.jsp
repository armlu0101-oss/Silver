<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.*" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Inventario";
    String activeMenu = "inventario";
    List<Movimiento> movimientos = (List<Movimiento>) request.getAttribute("movimientos");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    List<Producto> bajoStock = (List<Producto>) request.getAttribute("bajoStock");
    String msg = (String) session.getAttribute("mensajeMovimiento");
    String msgTipo = (String) session.getAttribute("mensajeMovimientoTipo");
    session.removeAttribute("mensajeMovimiento");
    session.removeAttribute("mensajeMovimientoTipo");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Movimientos de inventario</h5>
    <button class="btn btn-silver" data-bs-toggle="modal" data-bs-target="#modalMovimiento">
        <i class="bi bi-plus-lg"></i> Registrar movimiento
    </button>
</div>

<% if (msg != null) {%>
<script>
    window.addEventListener('DOMContentLoaded', () => {
        Swal.fire({
            icon: '<%= "success".equals(msgTipo) ? "success" : "error"%>',
            title: '<%= msg.replace("'", "\\'")%>',
            confirmButtonColor: '#d4af37'
        });
    });
</script>
<% } %>

<!-- ALERTAS AUTOMATICAS -->
<% if (!bajoStock.isEmpty()) { %>
<% for (Producto p : bajoStock) {
        boolean agotado = p.getCantidad() <= 0;
%>
<div class="alert <%= agotado ? "alert-stock-agotado" : "alert-stock-bajo"%> mb-2 py-2">
    <i class="bi <%= agotado ? "bi-x-octagon-fill" : "bi-exclamation-triangle-fill"%>"></i>
    <strong><%= p.getNombre()%></strong>
    &mdash; existencia actual: <%= p.getCantidad()%>, stock minimo: <%= p.getStockMinimo()%>
    <%= agotado ? " (AGOTADO, requiere reposicion urgente)" : " (por agotarse, requiere reposicion)"%>
</div>
<% } %>
<% } else { %>
<div class="alert alert-success py-2"><i class="bi bi-check-circle-fill"></i> Todos los productos tienen existencia suficiente.</div>
<% } %>

<div class="kpi-card p-0 mt-3">
    <table class="table table-silver mb-0">
        <thead><tr><th>Fecha</th><th>Producto</th><th>Tipo</th><th>Cantidad</th><th>Motivo</th><th>Usuario</th></tr></thead>
        <tbody>
            <% if (movimientos.isEmpty()) { %>
            <tr><td colspan="6"><div class="estado-vacio"><i class="bi bi-arrow-left-right"></i>Aun no se han registrado movimientos.</div></td></tr>
            <% } %>
            <% for (Movimiento m : movimientos) {
                    String colorTipo;

                    if ("ENTRADA".equals(m.getTipo())) {
                        colorTipo = "text-success";
                    } else if ("SALIDA".equals(m.getTipo())) {
                        colorTipo = "text-danger";
                    } else {
                        colorTipo = "text-warning";
                    }
            %>
            <tr>
                <td><%= m.getFecha()%></td>
                <td><%= m.getProductoNombre()%></td>
                <td class="<%= colorTipo%> fw-semibold"><%= m.getTipo()%></td>
                <td><%= m.getCantidad()%></td>
                <td><%= m.getMotivo() != null ? m.getMotivo() : "-"%></td>
                <td><%= m.getUsuarioNombre()%></td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<div class="modal fade" id="modalMovimiento" tabindex="-1">
    <div class="modal-dialog">
        <form class="modal-content" method="post" action="${pageContext.request.contextPath}/movimientos">
            <div class="modal-header">
                <h5 class="modal-title">Registrar movimiento</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-2">
                    <label class="form-label">Producto</label>
                    <select name="productoId" class="form-select" required>
                        <% for (Producto p : productos) {%>
                        <option value="<%= p.getId()%>"><%= p.getNombre()%> (existencia: <%= p.getCantidad()%>)</option>
                        <% }%>
                    </select>
                </div>
                <div class="mb-2">
                    <label class="form-label">Tipo de movimiento</label>
                    <select name="tipo" class="form-select" required>
                        <option value="ENTRADA">Entrada</option>
                        <option value="SALIDA">Salida</option>
                        <option value="AJUSTE">Ajuste</option>
                    </select>
                </div>
                <div class="mb-2">
                    <label class="form-label">Cantidad</label>
                    <input type="number" name="cantidad" min="1" class="form-control" required>
                </div>
                <div class="mb-2">
                    <label class="form-label">Motivo</label>
                    <input type="text" name="motivo" class="form-control" placeholder="Ej. Compra a proveedor, merma, conteo fisico...">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" class="btn btn-silver">Registrar</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
