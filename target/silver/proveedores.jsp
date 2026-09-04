<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.Proveedor" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Proveedores";
    String activeMenu = "proveedores";
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Proveedores</h5>
    <button class="btn btn-silver" data-bs-toggle="modal" data-bs-target="#modalProveedor" onclick="nuevoProveedor()">
        <i class="bi bi-plus-lg"></i> Nuevo proveedor
    </button>
</div>

<div class="kpi-card p-0">
<table class="table table-silver mb-0">
    <thead><tr><th>Nombre</th><th>Telefono</th><th>Correo</th><th>Direccion</th><th></th></tr></thead>
    <tbody>
    <% if (proveedores.isEmpty()) { %>
        <tr><td colspan="5"><div class="estado-vacio"><i class="bi bi-truck"></i>Aun no hay proveedores registrados.</div></td></tr>
    <% } %>
    <% for (Proveedor p : proveedores) { %>
        <tr>
            <td><%= p.getNombre() %></td>
            <td><%= p.getTelefono() != null ? p.getTelefono() : "-" %></td>
            <td><%= p.getCorreo() != null ? p.getCorreo() : "-" %></td>
            <td><%= p.getDireccion() != null ? p.getDireccion() : "-" %></td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-dark"
                    onclick="editarProveedor(<%= p.getId() %>, '<%= p.getNombre().replace("'", "&#39;") %>', '<%= p.getTelefono()!=null?p.getTelefono():"" %>', '<%= p.getCorreo()!=null?p.getCorreo():"" %>', '<%= p.getDireccion()!=null?p.getDireccion().replace("'", "&#39;"):"" %>')">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="desactivarProveedor(<%= p.getId() %>)">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    <% } %>
    </tbody>
</table>
</div>

<div class="modal fade" id="modalProveedor" tabindex="-1">
  <div class="modal-dialog">
    <form class="modal-content" method="post" action="${pageContext.request.contextPath}/proveedores">
      <div class="modal-header">
        <h5 class="modal-title" id="tituloProveedor">Nuevo proveedor</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" name="id" id="p_id">
        <div class="mb-2"><label class="form-label">Nombre</label><input type="text" name="nombre" id="p_nombre" class="form-control" required></div>
        <div class="mb-2"><label class="form-label">Telefono</label><input type="text" name="telefono" id="p_telefono" class="form-control"></div>
        <div class="mb-2"><label class="form-label">Correo</label><input type="email" name="correo" id="p_correo" class="form-control"></div>
        <div class="mb-2"><label class="form-label">Direccion</label><input type="text" name="direccion" id="p_direccion" class="form-control"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-silver">Guardar</button>
      </div>
    </form>
  </div>
</div>

<form id="formDesactivarProveedor" method="post" action="${pageContext.request.contextPath}/proveedores">
    <input type="hidden" name="accion" value="desactivar">
    <input type="hidden" name="id" id="dp_id">
</form>

<script>
function nuevoProveedor() {
    document.getElementById('tituloProveedor').innerText = 'Nuevo proveedor';
    document.querySelector('#modalProveedor form').reset();
    document.getElementById('p_id').value = '';
}
function editarProveedor(id, nombre, telefono, correo, direccion) {
    document.getElementById('tituloProveedor').innerText = 'Editar proveedor';
    document.getElementById('p_id').value = id;
    document.getElementById('p_nombre').value = nombre;
    document.getElementById('p_telefono').value = telefono;
    document.getElementById('p_correo').value = correo;
    document.getElementById('p_direccion').value = direccion;
    new bootstrap.Modal(document.getElementById('modalProveedor')).show();
}
function desactivarProveedor(id) {
    Swal.fire({
        title: 'Desactivar proveedor?', icon: 'warning', showCancelButton: true,
        confirmButtonColor: '#d4af37', confirmButtonText: 'Si, desactivar'
    }).then(r => {
        if (r.isConfirmed) {
            document.getElementById('dp_id').value = id;
            document.getElementById('formDesactivarProveedor').submit();
        }
    });
}
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
