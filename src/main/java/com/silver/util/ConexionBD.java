package com.silver.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para obtener conexiones JDBC a la base de datos MySQL "silver".
 * El sistema trabaja 100% local, sin necesidad de internet.
 */
public class ConexionBD {

private static final String URL =
    "jdbc:mysql://localhost:3306/silver?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City&characterEncoding=UTF-8";
private static final String USUARIO = "root";
private static final String PASSWORD = "1234";// Ajustar segun la instalacion local de MySQL

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver de MySQL", e);
        }
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
