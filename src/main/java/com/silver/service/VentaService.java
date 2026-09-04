package com.silver.service;

import com.silver.dao.VentaDAO;
import com.silver.model.DetalleVenta;
import com.silver.model.Venta;

import java.math.BigDecimal;
import java.sql.SQLException;

public class VentaService {

    private final VentaDAO ventaDAO = new VentaDAO();

    public static class Resultado {
        public final boolean exito;
        public final String mensaje;
        public final Integer ventaId;
        public Resultado(boolean exito, String mensaje, Integer ventaId) {
            this.exito = exito; this.mensaje = mensaje; this.ventaId = ventaId;
        }
    }

    public Resultado procesarVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            return new Resultado(false, "El carrito esta vacio.", null);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : venta.getDetalles()) {
            if (d.getCantidad() <= 0) {
                return new Resultado(false, "Cantidad invalida en el carrito.", null);
            }
            d.setSubtotal(d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())));
            total = total.add(d.getSubtotal());
        }
        venta.setTotal(total);

        try {
            int ventaId = ventaDAO.registrarVenta(venta);
            return new Resultado(true, "Venta registrada correctamente.", ventaId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, e.getMessage() != null ? e.getMessage() : "Error al registrar la venta.", null);
        }
    }

    public Venta obtenerVenta(int id) throws SQLException {
        return ventaDAO.obtenerVentaCompleta(id);
    }
}
