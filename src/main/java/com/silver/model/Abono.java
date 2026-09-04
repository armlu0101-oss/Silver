package com.silver.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Abono {
    private int id;
    private int creditoId;
    private BigDecimal monto;
    private Timestamp fecha;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreditoId() { return creditoId; }
    public void setCreditoId(int creditoId) { this.creditoId = creditoId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
}
