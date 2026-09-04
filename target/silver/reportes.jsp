<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String pageTitle = "Reportes";
    String activeMenu = "reportes";
    String ventasJson = (String) request.getAttribute("ventasJson");
    String movimientosJson = (String) request.getAttribute("movimientosJson");
    String masVendidosJson = (String) request.getAttribute("masVendidosJson");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="row g-3 mb-3">
    <div class="col-md-6">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-box-seam"></i></div>
            <div>
                <div class="kpi-value"><%= request.getAttribute("totalProductos") %></div>
                <div class="kpi-label">Productos activos</div>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-currency-dollar"></i></div>
            <div>
                <div class="kpi-value">$<%= request.getAttribute("valorInventario") %></div>
                <div class="kpi-label">Valor total del inventario</div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-md-6">
        <div class="kpi-card">
            <h6>Ventas por dia (ultimos 7 dias)</h6>
            <canvas id="chartVentas" height="200"></canvas>
        </div>
    </div>
    <div class="col-md-6">
        <div class="kpi-card">
            <h6>Entradas vs salidas (ultimos 7 dias)</h6>
            <canvas id="chartMovimientos" height="200"></canvas>
        </div>
    </div>
    <div class="col-md-12">
        <div class="kpi-card">
            <h6>Productos mas vendidos (ultimos 30 dias)</h6>
            <canvas id="chartMasVendidos" height="120"></canvas>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
const ventasData = <%= ventasJson %>;
const movimientosData = <%= movimientosJson %>;
const masVendidosData = <%= masVendidosJson %>;

new Chart(document.getElementById('chartVentas'), {
    type: 'line',
    data: {
        labels: ventasData.labels,
        datasets: [{
            label: 'Ventas ($)',
            data: ventasData.values,
            borderColor: '#d4af37',
            backgroundColor: 'rgba(212,175,55,0.15)',
            tension: 0.35,
            fill: true
        }]
    },
    options: { plugins: { legend: { display: false } } }
});

new Chart(document.getElementById('chartMovimientos'), {
    type: 'bar',
    data: {
        labels: movimientosData.labels,
        datasets: [
            { label: 'Entradas', data: movimientosData.entradas, backgroundColor: '#0d0d0d' },
            { label: 'Salidas', data: movimientosData.salidas, backgroundColor: '#d4af37' }
        ]
    }
});

new Chart(document.getElementById('chartMasVendidos'), {
    type: 'bar',
    data: {
        labels: masVendidosData.labels,
        datasets: [{ label: 'Unidades vendidas', data: masVendidosData.values, backgroundColor: '#d4af37' }]
    },
    options: { indexAxis: 'y', plugins: { legend: { display: false } } }
});
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
