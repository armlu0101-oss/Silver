<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.Categoria" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Categorias";
    String activeMenu = "categorias";
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Categorias</h5>
    <button class="btn btn-silver" data-bs-toggle="modal" data-bs-target="#modalCategoria" onclick="nuevaCategoria()">
        <i class="bi bi-plus-lg"></i> Nueva categoria
    </button>
</div>

<div class="kpi-card p-0">
<table class="table table-silver mb-0">
    <thead><tr><th>Nombre</th><th></th></tr></thead>
    <tbody>
    <% if (categorias.isEmpty()) { %>
        <tr><td colspan="2"><div class="estado-vacio"><i class="bi bi-tags"></i>Aun no hay categorias registradas.</div></td></tr>
    <% } %>
    <% for (Categoria c : categorias) { %>
        <tr>
            <td><%= c.getNombre() %></td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-dark" onclick="editarCategoria(<%= c.getId() %>, '<%= c.getNombre().replace("'", "&#39;") %>')">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="desactivarCategoria(<%= c.getId() %>)">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>
    <% } %>
    </tbody>
</table>
</div>

<div class="modal fade" id="modalCategoria" tabindex="-1">
  <div class="modal-dialog">
    <form class="modal-content" method="post" action="${pageContext.request.contextPath}/categorias">
      <div class="modal-header">
        <h5 class="modal-title" id="tituloCategoria">Nueva categoria</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" name="id" id="c_id">
        <label class="form-label">Nombre</label>
        <input type="text" name="nombre" id="c_nombre" class="form-control" required>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-silver">Guardar</button>
      </div>
    </form>
  </div>
</div>

<form id="formDesactivarCategoria" method="post" action="${pageContext.request.contextPath}/categorias">
    <input type="hidden" name="accion" value="desactivar">
    <input type="hidden" name="id" id="dc_id">
</form>

<script>
function nuevaCategoria() {
    document.getElementById('tituloCategoria').innerText = 'Nueva categoria';
    document.getElementById('c_id').value = '';
    document.getElementById('c_nombre').value = '';
}
function editarCategoria(id, nombre) {
    document.getElementById('tituloCategoria').innerText = 'Editar categoria';
    document.getElementById('c_id').value = id;
    document.getElementById('c_nombre').value = nombre;
    new bootstrap.Modal(document.getElementById('modalCategoria')).show();
}
function desactivarCategoria(id) {
    Swal.fire({
        title: 'Desactivar categoria?', icon: 'warning', showCancelButton: true,
        confirmButtonColor: '#d4af37', confirmButtonText: 'Si, desactivar'
    }).then(r => {
        if (r.isConfirmed) {
            document.getElementById('dc_id').value = id;
            document.getElementById('formDesactivarCategoria').submit();
        }
    });
}
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
