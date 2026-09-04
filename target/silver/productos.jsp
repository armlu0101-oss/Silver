<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.silver.model.*" %>
<%@ page import="java.util.List" %>
<%
    String pageTitle = "Productos";
    String activeMenu = "productos";
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    String filtroTexto = (String) request.getAttribute("filtroTexto");
    Object filtroCategoria = request.getAttribute("filtroCategoria");
%>
<%!
    // Convierte un Producto a JSON seguro para usarse dentro de un atributo HTML de comillas simples.
    private String jsonEsc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String toJson(com.silver.model.Producto p) {
        String json = "{"
                + "\"id\":" + p.getId() + ","
                + "\"nombre\":\"" + jsonEsc(p.getNombre()) + "\","
                + "\"codigo\":\"" + jsonEsc(p.getCodigo()) + "\","
                + "\"categoriaId\":" + (p.getCategoriaId() != null ? p.getCategoriaId() : "null") + ","
                + "\"marca\":\"" + jsonEsc(p.getMarca()) + "\","
                + "\"modelo\":\"" + jsonEsc(p.getModelo()) + "\","
                + "\"talla\":\"" + jsonEsc(p.getTalla()) + "\","
                + "\"color\":\"" + jsonEsc(p.getColor()) + "\","
                + "\"proveedorId\":" + (p.getProveedorId() != null ? p.getProveedorId() : "null") + ","
                + "\"precio\":" + p.getPrecio() + ","
                + "\"cantidad\":" + p.getCantidad() + ","
                + "\"stockMinimo\":" + p.getStockMinimo() + ","
                + "\"imagen\":\"" + jsonEsc(p.getImagen()) + "\""
                + "}";
        // Escapamos comillas simples para que no rompan el atributo HTML onclick='...'
        return json.replace("'", "&#39;");
    }
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Catalogo de productos</h5>
    <button class="btn btn-silver" data-bs-toggle="modal" data-bs-target="#modalProducto" onclick="nuevoProducto()">
        <i class="bi bi-plus-lg"></i> Nuevo producto
    </button>
</div>

<% if (request.getAttribute("error") != null) {%>
<div class="alert alert-danger"><%= request.getAttribute("error")%></div>
<% }%>

<!-- FILTROS -->
<form method="get" action="${pageContext.request.contextPath}/productos" class="row g-2 mb-3">
    <div class="col-md-4">
        <input type="text" name="q" class="form-control" placeholder="Buscar por nombre, codigo, marca o modelo..."
               value="<%= filtroTexto != null ? filtroTexto : ""%>">
    </div>
    <div class="col-md-3">
        <select name="categoriaId" class="form-select">
            <option value="">Todas las categorias</option>
            <% for (Categoria c : categorias) {%>
            <option value="<%= c.getId()%>" <%= (filtroCategoria != null && filtroCategoria.equals(c.getId())) ? "selected" : ""%>>
                <%= c.getNombre()%>
            </option>
            <% }%>
        </select>
    </div>
    <div class="col-md-3">
        <select name="estado" class="form-select">
            <option value="">Cualquier existencia</option>
            <option value="BAJO" <%= "BAJO".equals(request.getAttribute("filtroEstado")) ? "selected" : ""%>>Stock bajo</option>
            <option value="AGOTADO" <%= "AGOTADO".equals(request.getAttribute("filtroEstado")) ? "selected" : ""%>>Agotado</option>
        </select>
    </div>
    <div class="col-md-2">
        <button type="submit" class="btn btn-silver w-100"><i class="bi bi-search"></i> Filtrar</button>
    </div>
</form>

<!-- TABLA -->
<div class="kpi-card p-0">
    <table class="table table-silver mb-0 align-middle">
        <thead>
            <tr>
                <th>Img</th><th>Nombre</th><th>Codigo</th><th>Categoria</th><th>Marca</th>
                <th>Talla</th><th>Color</th><th>Precio</th><th>Existencia</th><th>Estado</th><th></th>
            </tr>
        </thead>
        <tbody>
            <% if (productos.isEmpty()) { %>
            <tr><td colspan="11">
                    <div class="estado-vacio"><i class="bi bi-inboxes"></i>No se encontraron productos con los filtros aplicados.</div>
                </td></tr>
                <% } %>
                <% for (Producto p : productos) {
                        String badge;

                        if ("AGOTADO".equals(p.getEstadoStock())) {
                            badge = "badge-stock-agotado";
                        } else if ("BAJO".equals(p.getEstadoStock())) {
                            badge = "badge-stock-bajo";
                        } else {
                            badge = "bg-success";
                        }
                        String imgSrc = (p.getImagen() != null && !p.getImagen().isBlank())
                                ? request.getContextPath() + "/assets/img/productos/" + p.getImagen()
                                : request.getContextPath() + "/assets/img/silver_logo_texto_black.png";
                %>
            <tr>
                <td><img src="<%= imgSrc%>" style="width:38px;height:38px;object-fit:cover;border-radius:6px;"></td>
                <td><%= p.getNombre()%></td>
                <td><%= p.getCodigo()%></td>
                <td><%= p.getCategoriaNombre() != null ? p.getCategoriaNombre() : "-"%></td>
                <td><%= p.getMarca() != null ? p.getMarca() : "-"%></td>
                <td><%= p.getTalla() != null ? p.getTalla() : "-"%></td>
                <td><%= p.getColor() != null ? p.getColor() : "-"%></td>
                <td>$<%= p.getPrecio()%></td>
                <td><%= p.getCantidad()%> (min. <%= p.getStockMinimo()%>)</td>
                <td><span class="badge <%= badge%>"><%= p.getEstadoStock()%></span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-dark" onclick='editarProducto(<%= toJson(p)%>)'>
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="desactivarProducto(<%= p.getId()%>)">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<!-- MODAL FORMULARIO -->
<div class="modal fade" id="modalProducto" tabindex="-1">
    <div class="modal-dialog">
        <form class="modal-content" method="post" action="${pageContext.request.contextPath}/productos" enctype="multipart/form-data">
            <div class="modal-header">
                <h5 class="modal-title" id="modalProductoTitulo">Nuevo producto</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="id" id="f_id">
                <input type="hidden" name="imagenActual" id="f_imagenActual">
                <div class="mb-2">
                    <label class="form-label">Nombre</label>
                    <input type="text" name="nombre" id="f_nombre" class="form-control" required>
                </div>
                <div class="row">
                    <div class="col-6 mb-2">
                        <label class="form-label">Codigo</label>
                        <input type="text" name="codigo" id="f_codigo" class="form-control" required>
                    </div>
                    <div class="col-6 mb-2">
                        <label class="form-label">Categoria</label>
                        <select name="categoriaId" id="f_categoriaId" class="form-select">
                            <option value="">-- Ninguna --</option>
                            <% for (Categoria c : categorias) {%>
                            <option value="<%= c.getId()%>"><%= c.getNombre()%></option>
                            <% } %>
                        </select>
                    </div>
                </div>
                <div class="row">
                    <div class="col-6 mb-2">
                        <label class="form-label">Marca</label>
                        <input type="text" name="marca" id="f_marca" class="form-control">
                    </div>
                    <div class="col-6 mb-2">
                        <label class="form-label">Modelo</label>
                        <input type="text" name="modelo" id="f_modelo" class="form-control">
                    </div>
                </div>
                <div class="row">
                    <div class="col-6 mb-2">
                        <label class="form-label">Talla</label>
                        <input type="text" name="talla" id="f_talla" class="form-control">
                    </div>
                    <div class="col-6 mb-2">
                        <label class="form-label">Color</label>
                        <input type="text" name="color" id="f_color" class="form-control">
                    </div>
                </div>
                <div class="mb-2">
                    <label class="form-label">Proveedor</label>
                    <select name="proveedorId" id="f_proveedorId" class="form-select">
                        <option value="">-- Ninguno --</option>
                        <% for (Proveedor pr : proveedores) {%>
                        <option value="<%= pr.getId()%>"><%= pr.getNombre()%></option>
                        <% }%>
                    </select>
                </div>
                <div class="row">
                    <div class="col-4 mb-2">
                        <label class="form-label">Precio</label>
                        <input type="number" step="0.01" name="precio" id="f_precio" class="form-control" required>
                    </div>
                    <div class="col-4 mb-2">
                        <label class="form-label">Existencia</label>
                        <input type="number" name="cantidad" id="f_cantidad" class="form-control" required>
                    </div>
                    <div class="col-4 mb-2">
                        <label class="form-label">Stock minimo</label>
                        <input type="number" name="stockMinimo" id="f_stockMinimo" class="form-control" required>
                    </div>
                </div>
                <div class="mb-2">
                    <label class="form-label">Imagen del producto</label>
                    <input type="file" name="imagen" accept="image/*" class="form-control">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" class="btn btn-silver">Guardar</button>
            </div>
        </form>
    </div>
</div>

<form id="formDesactivar" method="post" action="${pageContext.request.contextPath}/productos">
    <input type="hidden" name="accion" value="desactivar">
    <input type="hidden" name="id" id="d_id">
</form>

<script>
    function nuevoProducto() {
        document.getElementById('modalProductoTitulo').innerText = 'Nuevo producto';
        document.querySelector('#modalProducto form').reset();
        document.getElementById('f_id').value = '';
        document.getElementById('f_imagenActual').value = '';
    }

    function editarProducto(p) {
        document.getElementById('modalProductoTitulo').innerText = 'Editar producto';
        document.getElementById('f_id').value = p.id;
        document.getElementById('f_nombre').value = p.nombre;
        document.getElementById('f_codigo').value = p.codigo;
        document.getElementById('f_categoriaId').value = p.categoriaId || '';
        document.getElementById('f_marca').value = p.marca || '';
        document.getElementById('f_modelo').value = p.modelo || '';
        document.getElementById('f_talla').value = p.talla || '';
        document.getElementById('f_color').value = p.color || '';
        document.getElementById('f_proveedorId').value = p.proveedorId || '';
        document.getElementById('f_precio').value = p.precio;
        document.getElementById('f_cantidad').value = p.cantidad;
        document.getElementById('f_stockMinimo').value = p.stockMinimo;
        document.getElementById('f_imagenActual').value = p.imagen || '';
        new bootstrap.Modal(document.getElementById('modalProducto')).show();
    }

    function desactivarProducto(id) {
        Swal.fire({
            title: 'Desactivar producto?',
            text: 'El producto dejara de aparecer en el catalogo activo.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d4af37',
            confirmButtonText: 'Si, desactivar'
        }).then((r) => {
            if (r.isConfirmed) {
                document.getElementById('d_id').value = id;
                document.getElementById('formDesactivar').submit();
            }
        });
    }
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
