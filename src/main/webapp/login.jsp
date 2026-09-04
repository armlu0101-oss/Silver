<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SILVER | Iniciar sesion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/silver-theme.css" rel="stylesheet">
</head>
<body>

<div class="silver-login-wrapper">
    <div class="silver-login-card">

        <!-- Logo con el toro sobre fondo oscuro: se usa la version blanca invertida -->
        <img src="${pageContext.request.contextPath}/assets/img/silver_logo_toro_white.png"
             alt="SILVER" class="silver-login-logo">

        <div class="silver-login-tagline">Gestion inteligente para tu tienda</div>

        <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-danger py-2" role="alert" style="font-size:0.85rem;">
            <i class="bi bi-exclamation-triangle-fill"></i> <%= request.getAttribute("error") %>
        </div>
        <% } %>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="mb-3">
                <label for="usuario" class="form-label">Usuario</label>
                <input type="text" class="form-control" id="usuario" name="usuario" required autofocus>
            </div>
            <div class="mb-4">
                <label for="password" class="form-label">Contrasena</label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>
            <button type="submit" class="btn btn-silver w-100 py-2">
                <i class="bi bi-box-arrow-in-right"></i> INGRESAR
            </button>
        </form>
    </div>
</div>

</body>
</html>
