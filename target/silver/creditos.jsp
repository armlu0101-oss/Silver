<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.*" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Creditos";
    String activeMenu = "creditos";
    List<Credito> creditos = (List<Credito>) request.getAttribute("creditos");
    List<Locatario> locatarios = (List<Locatario>) request.getAttribute("locatarios");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    Object totalPendiente = request.getAttribute("totalPendiente");
%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="row g-3 mb-3">
    <div class="col-md-4">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-cash-coin"></i></div>
            <div>
                <div class="kpi-value">$<%= totalPendiente%></div>
                <div class="kpi-label">Total pendiente por cobrar</div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="kpi-card d-flex align-items-center gap-3">
            <div class="kpi-icon"><i class="bi bi-people-fill"></i></div>
            <div>
                <div class="kpi-value"><%= creditos.size()%></div>
                <div class="kpi-label">Creditos activos</div>
            </div>
        </div>
    </div>
    <div class="col-md-4 d-flex align-items-center justify-content-end">
        <button class="btn btn-silver btn-lg" data-bs-toggle="modal" data-bs-target="#modalEntrega">
            <i class="bi bi-truck"></i> Entregar mercancia a credito
        </button>
    </div>
</div>

<div class="kpi-card p-0">
    <table class="table table-silver mb-0">
        <thead><tr><th>Fecha</th><th>Locatario</th><th>Total</th><th>Saldo</th><th>Estado</th><th></th></tr></thead>
        <tbody>
            <% if (creditos.isEmpty()) { %>
            <tr><td colspan="6"><div class="estado-vacio"><i class="bi bi-credit-card"></i>No hay creditos activos por el momento.</div></td></tr>
            <% } %>
            <% for (Credito c : creditos) {
                    String badge;

                    switch (c.getEstado()) {
                        case "PAGADO":
                            badge = "bg-success";
                            break;

                        case "VENCIDO":
                            badge = "badge-stock-agotado";
                            break;

                        default:
                            badge = "badge-stock-bajo";
                            break;
                    }
            %>
            <tr>
                <td><%= c.getFecha()%></td>
                <td><%= c.getLocatarioNombre()%></td>
                <td>$<%= c.getTotal()%></td>
                <td>$<%= c.getSaldo()%></td>
                <td><span class="badge <%= badge%>"><%= c.getEstado()%></span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-dark" 
                            onclick="console.log('ID CREDITO:', <%= c.getId()%>); verCredito(<%= c.getId()%>)">
                        <i class="bi bi-eye"></i> Ver / Abonar
                    </button>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<!-- MODAL: ENTREGAR MERCANCIA -->
<div class="modal fade" id="modalEntrega" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Entregar mercancia a credito</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <label class="form-label">Locatario</label>
                <select id="entregaLocatarioId" class="form-select mb-3">
                    <% for (Locatario l : locatarios) {%>
                    <option value="<%= l.getId()%>"><%= l.getNombre()%></option>
                    <% } %>
                </select>

                <label class="form-label">Producto</label>
                <div class="input-group mb-3">
                    <select id="entregaProductoId" class="form-select">
                        <% for (Producto p : productos) {%>
                        <option value="<%= p.getId()%>" data-precio="<%= p.getPrecio()%>" data-max="<%= p.getCantidad()%>">
                            <%= p.getNombre()%> (existencia: <%= p.getCantidad()%>) - $<%= p.getPrecio()%>
                        </option>
                        <% }%>
                    </select>
                    <input type="number" id="entregaCantidad" class="form-control" style="max-width:100px;" value="1" min="1">
                    <button type="button" class="btn btn-outline-dark" onclick="agregarItemCredito()"> + </button>     
                </div>

                <table class="table table-sm">
                    <thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th><th></th></tr></thead>
                    <tbody id="carritoCreditoBody"></tbody>
                </table>
                <h5 class="text-end">Total: <span id="totalCredito">$0.00</span></h5>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" class="btn btn-silver" onclick="generarCredito()"> Generar Credito</button>
            </div>
        </div>
    </div>
</div>

<!-- MODAL: VER / ABONAR -->
<div class="modal fade" id="modalVerCredito" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Detalle del credito</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body" id="detalleCreditoBody">Cargando...</div>
            <div class="modal-footer">
                <div class="input-group">
                    <input type="number" step="0.01" id="montoAbono" class="form-control" placeholder="Monto a abonar">
                    <button class="btn btn-silver" onclick="registrarAbono()">Registrar abono</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const CONTEXT = "${pageContext.request.contextPath}";
    console.log("CONTEXT ACTUAL:", CONTEXT);

    let carritoCredito = [];
    let creditoActualId = null;

    function agregarItemCredito() {

        const select = document.getElementById("entregaProductoId");
        const option = select.options[select.selectedIndex];

        const item = {
            productoId: Number(option.value),
            nombre: option.text.split(" (existencia:")[0],
            cantidad: Number(document.getElementById("entregaCantidad").value),
            precio: Number(option.dataset.precio)
        };

        console.log("ITEM A INSERTAR");
        console.table(item);

        carritoCredito.push(item);

        console.log("CARRITO");
        console.table(carritoCredito);

        renderCarritoCredito();
    }
    function renderCarritoCredito() {

        console.log("QUIEN LLAMA RENDER CREDITO");
        console.log("CARRITO ACTUAL:", carritoCredito);

        const body = document.getElementById("carritoCreditoBody");

        let html = "";
        let total = 0;

        carritoCredito.forEach((item, index) => {

            console.log("PINTANDO ITEM:", item);

            let subtotal = item.cantidad * item.precio;

            total += subtotal;

            html +=
                    "<tr>" +
                    "<td>" + item.nombre + "</td>" +
                    "<td>" + item.cantidad + "</td>" +
                    "<td>$" + item.precio.toFixed(2) + "</td>" +
                    "<td>$" + subtotal.toFixed(2) + "</td>" +
                    "<td>" +
                    "<button type='button' class='btn btn-sm btn-danger' onclick='quitarItemCredito(" + index + ")'>" +
                    "-" +
                    "</button>" +
                    "</td>" +
                    "</tr>";
        });


        console.log("HTML GENERADO:");
        console.log(html);

        body.innerHTML = html;

        document.getElementById("totalCredito").innerHTML =
                "$" + total.toFixed(2);

        console.log("TOTAL CREDITO:", total);
    }
    function quitarItemCredito(idx) {
        carritoCredito.splice(idx, 1);
        renderCarritoCredito();
    }

    function generarCredito() {
        if (carritoCredito.length === 0) {
            Swal.fire({icon: 'info', title: 'Agrega al menos un producto', confirmButtonColor: '#d4af37'});
            return;
        }
        const payload = {
            locatarioId: parseInt(document.getElementById('entregaLocatarioId').value),
            items: carritoCredito.map(i => ({productoId: i.productoId, cantidad: i.cantidad}))
        };
        fetch('${pageContext.request.contextPath}/creditos?accion=entregar', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        })
                .then(r => r.json())
                .then(data => {
                    if (data.exito) {
                        Swal.fire({icon: 'success', title: 'Credito #' + data.creditoId + ' generado', confirmButtonColor: '#d4af37'})
                                .then(() => location.reload());
                    } else {
                        Swal.fire({icon: 'error', title: 'No se pudo generar el credito', text: data.mensaje, confirmButtonColor: '#d4af37'});
                    }
                });
    }
   function verCredito(id) {

    console.log("ID RECIBIDO:", id);

    if (id == null || id === "") {
        Swal.fire({
            icon: "error",
            title: "Error",
            text: "No se recibió el ID del crédito"
        });
        return;
    }

    creditoActualId = id;

    const detalle = document.getElementById("detalleCreditoBody");
    detalle.innerHTML = "<p>Cargando...</p>";

    const modal = new bootstrap.Modal(
        document.getElementById("modalVerCredito")
    );

    modal.show();

    const url = CONTEXT + "/creditos?accion=detalleJson&id=" + id;

    console.log("URL:", url);

    fetch(url)
        .then(response => {

            console.log("STATUS:", response.status);

            if (!response.ok) {
                throw new Error("HTTP " + response.status);
            }

            return response.json();

        })
        .then(credito => {

            console.log("JSON:", credito);

            if (!credito.exito) {
                detalle.innerHTML =
                    "<p class='text-danger'>No se encontró el crédito.</p>";
                return;
            }

            let html = "";

            html += "<p>";
            html += "<strong>Locatario:</strong> " + credito.locatario;
            html += "</p>";

            html += "<p>";
            html += "<strong>Total:</strong> $" + credito.total;
            html += "<br>";
            html += "<strong>Saldo:</strong> $" + credito.saldo;
            html += "<br>";
            html += "<strong>Estado:</strong> " + credito.estado;
            html += "</p>";

            html += "<table class='table table-sm'>";
            html += "<thead>";
            html += "<tr>";
            html += "<th>Producto</th>";
            html += "<th>Cantidad</th>";
            html += "<th>Precio</th>";
            html += "</tr>";
            html += "</thead>";
            html += "<tbody>";

            if (credito.detalles && credito.detalles.length > 0) {

                credito.detalles.forEach(function(d){

                    html += "<tr>";
                    html += "<td>" + d.nombre + "</td>";
                    html += "<td>" + d.cantidad + "</td>";
                    html += "<td>$" + d.precio + "</td>";
                    html += "</tr>";

                });

            } else {

                html += "<tr>";
                html += "<td colspan='3' class='text-center'>Sin productos</td>";
                html += "</tr>";

            }

            html += "</tbody>";
            html += "</table>";

            console.log("HTML GENERADO:");
            console.log(html);

            detalle.innerHTML = html;

        })
        .catch(error => {

            console.error(error);

            detalle.innerHTML =
                "<p class='text-danger'>Error al cargar el crédito.</p>";

            Swal.fire({
                icon: "error",
                title: "Error",
                text: "No fue posible cargar el detalle del crédito."
            });

        });

}
    function registrarAbono() {
        const monto = document.getElementById('montoAbono').value;
        if (!monto || parseFloat(monto) <= 0) {
            Swal.fire({icon: 'warning', title: 'Ingresa un monto valido', confirmButtonColor: '#d4af37'});
            return;
        }
        const params = new URLSearchParams();
        params.append('accion', 'abonar');
        params.append('creditoId', creditoActualId);
        params.append('monto', monto);
        fetch('${pageContext.request.contextPath}/creditos', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: params.toString()
        })
                .then(r => r.json())
                .then(data => {
                    if (data.exito) {
                        Swal.fire({icon: 'success', title: 'Abono registrado', confirmButtonColor: '#d4af37'}).then(() => location.reload());
                    } else {
                        Swal.fire({icon: 'error', title: 'No se pudo registrar el abono', text: data.mensaje, confirmButtonColor: '#d4af37'});
                    }
                });
    }

    document.addEventListener("DOMContentLoaded", () => {

        console.log("MODULO CREDITOS CARGADO");
        renderCarritoCredito();
    });
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
