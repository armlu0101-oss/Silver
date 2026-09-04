<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String pageTitle = "Ventas POS";
    String activeMenu = "ventas";
%>
<%@ include file="/WEB-INF/includes/layout_top.jspf" %>

<div class="row g-3">
    <!-- BUSCADOR Y RESULTADOS -->
    <div class="col-md-6">
        <div class="kpi-card">
            <label class="form-label">Buscar producto (nombre, codigo o marca)</label>
            <input type="text" id="buscador" class="form-control mb-3" placeholder="Escribe para buscar...">
            <div id="resultados" style="max-height:420px; overflow-y:auto;"></div>
        </div>
    </div>

    <!-- CARRITO -->
    <div class="col-md-6">
        <div class="kpi-card d-flex flex-column" style="min-height:480px;">
            <h6 class="mb-3"><i class="bi bi-cart-check-fill"></i> Carrito de venta</h6>
            <div class="table-responsive flex-grow-1">
                <table class="table table-silver mb-0">
                    <thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th><th></th></tr></thead>
                    <tbody id="carritoBody"></tbody>
                </table>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
                <h4 class="mb-0">Total: <span id="totalVenta">$0.00</span></h4>
                <button class="btn btn-silver btn-lg" id="btnCobrar" onclick="registrarVenta()">
                    <i class="bi bi-check-circle-fill"></i> Cobrar
                </button>
            </div>
        </div>

        <!-- Reimpresion -->
        <div class="kpi-card mt-3">
            <h6 class="mb-2"><i class="bi bi-printer-fill"></i> Reimprimir ticket</h6>
            <div class="input-group">
                <input type="number" id="ventaIdReimprimir" class="form-control" placeholder="No. de venta">
                <button class="btn btn-outline-dark" onclick="reimprimirTicket()">Reimprimir</button>
            </div>
        </div>
    </div>
</div>

<script>
    const CONTEXT = "${pageContext.request.contextPath}";

    let carrito = []; // {productoId, nombre, cantidad, precio}
    let productosBusqueda = [];

    const buscador = document.getElementById('buscador');
    const resultadosDiv = document.getElementById('resultados');

    let debounce;
    buscador.addEventListener('input', () => {
        clearTimeout(debounce);
        const q = buscador.value.trim();
        if (q.length < 1) {
            resultadosDiv.innerHTML = '';
            return;
        }
        debounce = setTimeout(() => buscarProductos(q), 250);
    });

    function buscarProductos(q) {

        fetch(CONTEXT + "/productos?accion=buscarJson&q=" + encodeURIComponent(q))
                .then(function (response) {

                    if (!response.ok) {
                        throw new Error("Error al consultar productos");
                    }

                    return response.json();

                })
                .then(function (productos) {

                    productosBusqueda = productos;
                    console.log(productos);

                    var html = "";

                    if (productos.length === 0) {

                        html = '<p class="text-muted">Sin resultados.</p>';

                    } else {

                        productos.forEach(function (p) {

                            html += '<div class="d-flex justify-content-between align-items-center border-bottom py-2">';

                            html += '<div>';

                            html += '<div class="fw-semibold">' + p.nombre + '</div>';

                            html += '<small class="text-muted">';

                            html += 'Cod: ' + p.codigo;

                            if (p.talla) {
                                html += ' · Talla ' + p.talla;
                            }

                            html += ' · Existencia: ' + p.cantidad;

                            html += '</small>';

                            html += '</div>';

                            html += '<div class="text-end">';

                            html += '<div class="fw-semibold">$' + Number(p.precio).toFixed(2) + '</div>';

                            html += '<button class="btn btn-sm btn-silver mt-1"';

                            html += ' onclick="agregarProducto(\'' + p.id + '\')"';
                            if (p.cantidad <= 0) {
                                html += ' disabled';
                            }

                            html += '>';

                            html += '<i class="bi bi-plus-lg"></i> Agregar';

                            html += '</button>';

                            html += '</div>';

                            html += '</div>';

                        });

                    }

                    resultadosDiv.innerHTML = html;

                })
                .catch(function (error) {

                    console.error(error);

                    resultadosDiv.innerHTML =
                            '<div class="alert alert-danger">No se pudieron cargar los productos.</div>';

                });

    }

    function agregarProducto(id) {

        const producto = productosBusqueda.find(
                p => Number(p.id) === Number(id)
        );

        console.log("PRODUCTO SELECCIONADO:", producto);

        if (producto) {
            agregarAlCarrito(producto);
        }
    }
    function agregarAlCarrito(p) {
        const existente = carrito.find(
                i => Number(i.productoId) === Number(p.id)
        );

        if (existente) {

            if (existente.cantidad < p.cantidad) {
                existente.cantidad++;
            } else {
                Swal.fire({
                    icon: 'warning',
                    title: 'Sin más existencia disponible',
                    confirmButtonColor: '#d4af37'
                });
            }

        } else {

            carrito.push({
                productoId: Number(p.id),
                nombre: p.nombre,
                cantidad: 1,
                precio: Number(p.precio),
                maxCantidad: Number(p.cantidad)
            });

        }

        renderCarrito();
    }

    function cambiarCantidad(idx, delta) {
        const item = carrito[idx];
        const nueva = item.cantidad + delta;
        if (nueva < 1) {
            carrito.splice(idx, 1);
        } else if (nueva <= item.maxCantidad) {
            item.cantidad = nueva;
        }
        renderCarrito();
    }

    function quitarDelCarrito(idx) {
        carrito.splice(idx, 1);
        renderCarrito();
    }

    function renderCarrito() {

        console.log("Carrito:", carrito);

        const body = document.getElementById('carritoBody');

        console.log("ANTES:", body.innerHTML);

        let total = 0;

        body.innerHTML = carrito.map((item, idx) => {

            console.log("ITEM DENTRO DEL MAP:", item);
            console.log("NOMBRE:", item.nombre);
            console.log("PRECIO:", item.precio);
            console.log("INDEX:", idx);

            const subtotal = item.cantidad * item.precio;
            total += subtotal;

            return `
<tr>
    <td>\${item.nombre}</td>

    <td>
        <div class="input-group input-group-sm" style="width:110px;">
            <button class="btn btn-outline-dark" onclick="cambiarCantidad(\${idx},-1)">-</button>

            <input type="text" 
                   class="form-control text-center" 
                   value="\${item.cantidad}" 
                   readonly>

            <button class="btn btn-outline-dark" onclick="cambiarCantidad(\${idx},1)">+</button>
        </div>
    </td>

    <td>$\${Number(item.precio).toFixed(2)}</td>

    <td>$\${Number(subtotal).toFixed(2)}</td>

    <td>
        <button class="btn btn-sm btn-outline-danger" onclick="quitarDelCarrito(\${idx})">
            <i class="bi bi-x-lg"></i>
        </button>
    </td>

</tr>`;
        }).join('');

        console.log("DESPUES:", body.innerHTML);

        document.getElementById('totalVenta').innerText =
                '$' + total.toFixed(2);
    }
    function registrarVenta() {
        if (carrito.length === 0) {
            Swal.fire({icon: 'info', title: 'El carrito esta vacio', confirmButtonColor: '#d4af37'});
            return;
        }
        const payload = carrito.map(i => ({productoId: i.productoId, cantidad: i.cantidad}));

        fetch(CONTEXT + "/ventas", {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        })
                .then(r => r.json())
                .then(data => {
                    if (data.exito) {
                        Swal.fire({
                            icon: 'success',
                            title: 'Venta registrada (#' + data.ventaId + ')',
                            text: 'Enviando ticket a la impresora...',
                            confirmButtonColor: '#d4af37'
                        });
                        imprimirTicket(data.ventaId);
                        carrito = [];
                        renderCarrito();
                    } else {
                        Swal.fire({icon: 'error', title: 'No se pudo registrar la venta', text: data.mensaje, confirmButtonColor: '#d4af37'});
                    }
                });
    }

    function imprimirTicket(ventaId) {
        fetch(CONTEXT + "/ticket?ventaId=" + ventaId).then(r => r.json())
                .then(data => {
                    if (!data.exito) {
                        Swal.fire({icon: 'warning', title: 'Venta guardada, pero no se pudo imprimir', text: data.mensaje, confirmButtonColor: '#d4af37'});
                    }
                });
    }

    function reimprimirTicket() {
        const id = document.getElementById('ventaIdReimprimir').value;
        if (!id)
            return;
        imprimirTicket(id);
    }
</script>

<%@ include file="/WEB-INF/includes/layout_bottom.jspf" %>
