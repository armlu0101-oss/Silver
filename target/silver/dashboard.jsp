<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String pageTitle = "Dashboard";
    String activeMenu = "dashboard";
    List<Producto> bajoStock = (List<Producto>) request.getAttribute("bajoStock");
    List<Movimiento> recientes = (List<Movimiento>) request.getAttribute("movimientosRecientes");
    Map<String, java.math.BigDecimal> ventasSemana = (Map<String, java.math.BigDecimal>) request.getAttribute("ventasSemana");
    Map<String, int[]> movSemana = (Map<String, int[]>) request.getAttribute("movimientosSemana");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
<% } else { %>

<!-- ALERTAS AUTOMATICAS -->
<% if (bajoStock != null && !bajoStock.isEmpty()) {
     for (Producto p : bajoStock) {
        boolean agotado = p.getCantidad() <= 0;
%>
    <div class="alert <%= agotado ? "alert-stock-agotado" : "alert-stock-bajo" %> mb-2 py-2">
        <i class="bi <%= agotado ? "bi-x-octagon-fill" : "bi-exclamation-triangle-fill" %>"></i>
        <strong><%= p.getNombre() %></strong> &mdash; existencia: <%= p.getCantidad() %> (minimo <%= p.getStockMinimo() %>)
        <%= agotado ? " · AGOTADO" : " · por agotarse" %>
    </div>
<%  }
   } else { %>
    <div class="alert alert-success py-2"><i class="bi bi-check-circle-fill"></i> Todos los productos tienen existencia suficiente.</div>
<% } %>

<!-- KPIs -->
<div class="row g-3 mb-4 mt-1">
    <div class="col-md-3">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-box-seam"></i></div>
            <div><div class="kpi-value"><%= request.getAttribute("totalProductos") %></div><div class="kpi-label">Productos</div></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-currency-dollar"></i></div>
            <div><div class="kpi-value">$<%= request.getAttribute("valorInventario") %></div><div class="kpi-label">Valor inventario</div></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-cash-stack"></i></div>
            <div><div class="kpi-value">$<%= request.getAttribute("ventasHoy") %></div><div class="kpi-label">Ventas del dia</div></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-exclamation-triangle-fill"></i></div>
            <div><div class="kpi-value"><%= request.getAttribute("bajoStockCount") %></div><div class="kpi-label">Bajo stock</div></div>
        </div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-md-6">
        <div class="kpi-card">
            <canvas id="chartVentas" height="180"></canvas>
        </div>
    </div>
    <div class="col-md-6">
        <div class="kpi-card">
            <canvas id="chartMovimientos" height="180"></canvas>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-md-6">
        <div class="kpi-card">
            <h6><i class="bi bi-arrow-left-right"></i> Movimientos recientes</h6>
            <table class="table table-sm mb-0">
                <thead><tr><th>Fecha</th><th>Producto</th><th>Tipo</th><th>Cant.</th></tr></thead>
                <tbody>
                <% if (recientes != null) for (Movimiento m : recientes) { %>
                    <tr><td><%= m.getFecha() %></td><td><%= m.getProductoNombre() %></td><td><%= m.getTipo() %></td><td><%= m.getCantidad() %></td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <div class="col-md-6">
        <div class="kpi-card d-flex flex-column justify-content-center align-items-center text-center h-100">
            <i class="bi bi-credit-card-2-front-fill" style="font-size:2rem; color:var(--silver-gold);"></i>
            <div class="kpi-value mt-2">$<%= request.getAttribute("creditosPendientesTotal") %></div>
            <div class="kpi-label">Creditos pendientes (<%= request.getAttribute("creditosPendientesCount") %> activos)</div>
            <a href="${pageContext.request.contextPath}/creditos" class="btn btn-silver btn-sm mt-3">Ver creditos</a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
new Chart(document.getElementById('chartVentas'), {
    type: 'line',
    data: {
        labels: [<% for (String k : ventasSemana.keySet()) { %>'<%= k %>',<% } %>],
        datasets: [{
            label: 'Ventas ($)',
            data: [<% for (java.math.BigDecimal v : ventasSemana.values()) { %><%= v %>,<% } %>],
            borderColor: '#d4af37', backgroundColor: 'rgba(212,175,55,0.15)', tension: 0.35, fill: true
        }]
    },
    options: { plugins: { legend: { display: false } } }
});

new Chart(document.getElementById('chartMovimientos'), {
    type: 'bar',
    data: {
        labels: [<% for (String k : movSemana.keySet()) { %>'<%= k %>',<% } %>],
        datasets: [
            { label: 'Entradas', data: [<% for (int[] v : movSemana.values()) { %><%= v[0] %>,<% } %>], backgroundColor: '#0d0d0d' },
            { label: 'Salidas', data: [<% for (int[] v : movSemana.values()) { %><%= v[1] %>,<% } %>], backgroundColor: '#d4af37' }
        ]
    }
});
</script>

<% } %>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
