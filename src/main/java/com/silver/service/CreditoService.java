package com.silver.service;

import com.silver.dao.CreditoDAO;
import com.silver.model.Credito;
import com.silver.model.DetalleCredito;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CreditoService {

    private final CreditoDAO creditoDAO = new CreditoDAO();

    public static class Resultado {
        public final boolean exito;
        public final String mensaje;
        public final Integer creditoId;
        public Resultado(boolean exito, String mensaje, Integer creditoId) {
            this.exito = exito; this.mensaje = mensaje; this.creditoId = creditoId;
        }
    }

    public Resultado entregarMercancia(Credito credito, int usuarioId) {
        if (credito.getDetalles() == null || credito.getDetalles().isEmpty()) {
            return new Resultado(false, "Debe agregar al menos un producto.", null);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCredito d : credito.getDetalles()) {
            if (d.getCantidad() <= 0) return new Resultado(false, "Cantidad invalida.", null);
            total = total.add(d.getSubtotal());
        }
        credito.setTotal(total);

        try {
            int id = creditoDAO.registrarCredito(credito, usuarioId);
            return new Resultado(true, "Credito generado correctamente.", id);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, e.getMessage() != null ? e.getMessage() : "Error al generar el credito.", null);
        }
    }

    public Resultado registrarAbono(int creditoId, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return new Resultado(false, "El monto del abono debe ser mayor a cero.", null);
        }
        try {
            creditoDAO.registrarAbono(creditoId, monto);
            return new Resultado(true, "Abono registrado correctamente.", creditoId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Resultado(false, e.getMessage() != null ? e.getMessage() : "Error al registrar el abono.", null);
        }
    }

    public List<Credito> listarActivos() throws SQLException {
        return creditoDAO.listarActivos();
    }

    public Credito obtenerCompleto(int id) throws SQLException {
        return creditoDAO.obtenerCompleto(id);
    }

    public BigDecimal totalPendiente() throws SQLException {
        return creditoDAO.totalPendiente();
    }

    public int contarActivos() throws SQLException {
        return creditoDAO.contarActivos();
    }
}
