-- ============================================================
-- SILVER - Sistema de Gestion para Tiendas de Calzado
-- Base de datos completa (Fase 1: usuarios funcional y operativo,
-- el resto de tablas se deja creada para las siguientes fases)
-- ============================================================

CREATE DATABASE IF NOT EXISTS silver
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE silver;

-- ============================================================
-- USUARIOS
-- ============================================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,   -- Sin cifrado, tal como se especifico
    rol ENUM('ADMIN','VENDEDOR','ALMACEN') NOT NULL DEFAULT 'VENDEDOR',
    estatus ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usuario administrador de prueba (usuario: admin / password: admin123)
INSERT INTO usuarios (nombre, usuario, password, rol, estatus)
VALUES ('Administrador', 'admin', 'admin123', 'ADMIN', 'ACTIVO');

-- ============================================================
-- CATEGORIAS
-- ============================================================
CREATE TABLE categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    estatus ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

-- ============================================================
-- PROVEEDORES
-- ============================================================
CREATE TABLE proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(100),
    direccion VARCHAR(200),
    estatus ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

-- ============================================================
-- PRODUCTOS
-- ============================================================
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    categoria_id INT,
    proveedor_id INT,
    modelo VARCHAR(80),
    marca VARCHAR(80),
    talla VARCHAR(10),
    color VARCHAR(40),
    precio DECIMAL(10,2) NOT NULL DEFAULT 0,
    cantidad INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 5,
    imagen VARCHAR(255),
    estatus ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
);

-- ============================================================
-- VENTAS
-- ============================================================
CREATE TABLE ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE detalle_venta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES ventas(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- ============================================================
-- MOVIMIENTOS DE INVENTARIO
-- ============================================================
CREATE TABLE movimientos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT NOT NULL,
    tipo ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    cantidad INT NOT NULL,
    motivo VARCHAR(200),
    usuario_id INT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ============================================================
-- LOCATARIOS
-- ============================================================
CREATE TABLE locatarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    estatus ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

-- ============================================================
-- CREDITOS
-- ============================================================
CREATE TABLE creditos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    locatario_id INT NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado ENUM('PENDIENTE','PAGADO','VENCIDO') NOT NULL DEFAULT 'PENDIENTE',
    FOREIGN KEY (locatario_id) REFERENCES locatarios(id)
);

CREATE TABLE detalle_credito (
    id INT AUTO_INCREMENT PRIMARY KEY,
    credito_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (credito_id) REFERENCES creditos(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE abonos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    credito_id INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (credito_id) REFERENCES creditos(id)
);
