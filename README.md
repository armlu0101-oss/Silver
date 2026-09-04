# SILVER — Sistema de Gestion para Tiendas de Botas y Calzado

Sistema comercial tipo ERP/POS, 100% local (no requiere internet para operar),
desarrollado en Java 17 + JDBC + MySQL 8, con interfaz web estilo Shopify Admin.

---

## 1. Requisitos previos

Instala en la maquina donde correra SILVER (una sola vez):

| Componente | Version minima | Para que sirve |
|---|---|---|
| **JDK** | 17 | Compilar y ejecutar el sistema |
| **Apache Maven** | 3.9+ | Descargar dependencias y empaquetar el `.war` |
| **MySQL Server** | 8.0 | Base de datos local |
| **Apache Tomcat** | 10.1+ (Jakarta EE 10) | Servidor donde se despliega SILVER |

> Importante: Tomcat debe ser version **10.1 o superior** porque el proyecto usa
> `jakarta.servlet.*` (Jakarta EE), no `javax.servlet.*` (Java EE clasico).
> Tomcat 9 y anteriores NO son compatibles.

---

## 2. Crear la base de datos

```bash
mysql -u root -p < db/silver.sql
```

Esto crea la base `silver`, todas sus tablas, y un usuario administrador de prueba:

- **Usuario:** `admin`
- **Contrasena:** `admin123`

(Las contrasenas se guardan tal cual en la base de datos, sin cifrado, tal como se definio en los requisitos del sistema.)

---

## 3. Configurar la conexion a MySQL

Edita el archivo:

```
src/main/java/com/silver/util/ConexionBD.java
```

Y ajusta usuario/contrasena de tu instalacion local de MySQL:

```java
private static final String USUARIO = "root";
private static final String PASSWORD = "tu_password_aqui";
```

Si tu MySQL corre en otro puerto o servidor, ajusta tambien la URL.

---

## 4. Compilar el proyecto (genera el .war)

Desde la carpeta raiz del proyecto:

```bash
mvn clean package
```

Esto genera el archivo desplegable en:

```
target/silver.war
```

---

## 5. Desplegar en Tomcat

1. Copia `target/silver.war` a la carpeta `webapps/` de tu instalacion de Tomcat.
2. Inicia Tomcat (`startup.bat` en Windows o `./startup.sh` en Linux/Mac, dentro de `bin/`).
3. Tomcat descomprime el war automaticamente. Espera unos segundos.
4. Abre en el navegador:

```
http://localhost:8080/silver/
```

5. Inicia sesion con `admin` / `admin123`.

> Como el sistema es 100% local, tambien puedes acceder desde otras computadoras
> de la misma red usando la IP local del equipo que corre Tomcat, por ejemplo:
> `http://192.168.1.50:8080/silver/`

---

## 6. Configurar la impresora termica (tickets)

SILVER envia los tickets en formato **ESC/POS** usando la API estandar de
impresion de Java (`javax.print`), por lo que **no necesitas ningun driver
adicional**: solo que la impresora este instalada como impresora normal del
sistema operativo (USB, 58mm u 80mm).

**Windows:**
1. Conecta la impresora termica por USB.
2. Instala el driver que trae el fabricante (o usa el driver generico "Generic / Text Only").
3. En "Dispositivos e impresoras", verifica el nombre exacto de la impresora.

**Linux:**
1. Conecta la impresora y agregala con `CUPS` (`http://localhost:631`).
2. Verifica el nombre con `lpstat -p`.

Por defecto, SILVER imprime en la **impresora predeterminada** del sistema.
Si quieres usar una impresora especifica, se puede indicar su nombre exacto
al llamar el endpoint de impresion (parametro `impresora` en `/ticket`).

Si no hay impresora conectada, la venta se registra igual y el sistema solo
avisa que no pudo imprimir — nunca bloquea la operacion.

---

## 7. Estructura del proyecto

```
silver-project/
├── pom.xml
├── db/silver.sql                        -> script completo de base de datos
├── README.md                            -> este archivo
└── src/main/
    ├── java/com/silver/
    │   ├── util/        (ConexionBD, EscPosBuilder)
    │   ├── model/        (Usuario, Producto, Categoria, Proveedor, Movimiento,
    │   │                  Venta, DetalleVenta, Locatario, Credito, DetalleCredito, Abono)
    │   ├── dao/           (una clase DAO por entidad, con PreparedStatement)
    │   ├── service/       (reglas de negocio y validaciones)
    │   ├── controller/    (servlets, uno por modulo)
    │   └── filter/        (control de sesion)
    └── webapp/
        ├── assets/img/    (logos SILVER, en negro y version invertida en blanco)
        ├── css/silver-theme.css   (paleta negro/plata/dorado + alertas naranja/rojo)
        ├── WEB-INF/includes/     (layout comun: sidebar + navbar reutilizable)
        └── *.jsp          (una vista por modulo)
```

---

## 8. Modulos disponibles

| Modulo | Ruta | Descripcion |
|---|---|---|
| Login | `/login` | Autenticacion contra la tabla `usuarios` |
| Dashboard | `/dashboard` | KPIs, alertas de stock y graficos en tiempo real |
| Productos | `/productos` | Catalogo, busqueda, filtros, imagenes |
| Inventario | `/movimientos` | Entradas, salidas, ajustes y alertas automaticas |
| Ventas POS | `/ventas` | Punto de venta con carrito e impresion de ticket |
| Proveedores | `/proveedores` | CRUD de proveedores |
| Categorias | `/categorias` | CRUD de categorias |
| Creditos | `/creditos` | Entrega de mercancia a credito, abonos y saldos |
| Locatarios | `/locatarios` | CRUD de locatarios |
| Reportes | `/reportes` | Graficos de ventas, movimientos y productos top |

---

## 9. Notas de seguridad

- Todas las consultas usan `PreparedStatement` (protegido contra inyeccion SQL).
- El acceso a cualquier pagina, excepto el login, requiere sesion activa (`SesionFilter`).
- Las transacciones criticas (ventas, creditos, movimientos de inventario) son
  atomicas: si algo falla a la mitad, se revierte todo automaticamente.
- Tal como se definio en los requisitos, las contrasenas NO se cifran.
  Si en el futuro se requiere mayor seguridad, se recomienda migrar a hash
  (BCrypt) en `UsuarioDAO` / `UsuarioService`.
