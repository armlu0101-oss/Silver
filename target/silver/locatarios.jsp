<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.Locatario" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Locatarios";
    String activeMenu = "locatarios";
    List<Locatario> locatarios = (List<Locatario>) request.getAttribute("locatarios");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Locatarios</h5>
    <button class="btn btn-silver" data-bs-toggle="modal" data-bs-target="#modalLocatario" onclick="nuevoLocatario()">
        <i class="bi bi-plus-lg"></i> Nuevo locatario
    </button>
</div>

<div class="kpi-card p-0">
<table class="table table-silver mb-0">
    <thead><tr><th>Nombre</th><th>Telefono</th><th>Direccion</th><th></th></tr></thead>
    <tbody>
    <% if (locatarios.isEmpty()) { %>
        <tr><td colspan="4"><div class="estado-vacio"><i class="bi bi-people"></i>Aun no hay locatarios registrados.</div></td></tr>
    <% } %>
    <% for (Locatario l : locatarios) { %>
        <tr>
            <td><%= l.getNombre() %></td>
            <td><%= l.getTelefono() != null ? l.getTelefono() : "-" %></td>
            <td><%= l.getDireccion() != null ? l.getDireccion() : "-" %></td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-dark"
                    onclick="editarLocatario(<%= l.getId() %>, '<%= l.getNombre().replace("'", "&#39;") %>', '<%= l.getTelefono()!=null?l.getTelefono():"" %>', '<%= l.getDireccion()!=null?l.getDireccion().replace("'", "&#39;"):"" %>')">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="desactivarLocatario(<%= l.getId() %>)">
                    <i class="bi bi-trash"></i>
                </button>
                <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/creditos"><i class="bi bi-credit-card-2-front-fill"></i></a>
            </td>
        </tr>
    <% } %>
    </tbody>
</table>
</div>

<div class="modal fade" id="modalLocatario" tabindex="-1">
  <div class="modal-dialog">
    <form class="modal-content" method="post" action="${pageContext.request.contextPath}/locatarios">
      <div class="modal-header">
        <h5 class="modal-title" id="tituloLocatario">Nuevo locatario</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" name="id" id="l_id">
        <div class="mb-2"><label class="form-label">Nombre</label><input type="text" name="nombre" id="l_nombre" class="form-control" required></div>
        <div class="mb-2"><label class="form-label">Telefono</label><input type="text" name="telefono" id="l_telefono" class="form-control"></div>
        <div class="mb-2"><label class="form-label">Direccion</label><input type="text" name="direccion" id="l_direccion" class="form-control"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-silver">Guardar</button>
      </div>
    </form>
  </div>
</div>

<form id="formDesactivarLocatario" method="post" action="${pageContext.request.contextPath}/locatarios">
    <input type="hidden" name="accion" value="desactivar">
    <input type="hidden" name="id" id="dl_id">
</form>

<script>
function nuevoLocatario() {
    document.getElementById('tituloLocatario').innerText = 'Nuevo locatario';
    document.querySelector('#modalLocatario form').reset();
    document.getElementById('l_id').value = '';
}
function editarLocatario(id, nombre, telefono, direccion) {
    document.getElementById('tituloLocatario').innerText = 'Editar locatario';
    document.getElementById('l_id').value = id;
    document.getElementById('l_nombre').value = nombre;
    document.getElementById('l_telefono').value = telefono;
    document.getElementById('l_direccion').value = direccion;
    new bootstrap.Modal(document.getElementById('modalLocatario')).show();
}
function desactivarLocatario(id) {
    Swal.fire({
        title: 'Desactivar locatario?', icon: 'warning', showCancelButton: true,
        confirmButtonColor: '#d4af37', confirmButtonText: 'Si, desactivar'
    }).then(r => {
        if (r.isConfirmed) {
            document.getElementById('dl_id').value = id;
            document.getElementById('formDesactivarLocatario').submit();
        }
    });
}
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
