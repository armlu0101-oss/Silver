package com.silver.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Credito {
    private int id;
    private Timestamp fecha;
    private int locatarioId;
    private String locatarioNombre;
    private BigDecimal total;
    private BigDecimal saldo;
    private String estado; // PENDIENTE, PAGADO, VENCIDO
    private List<DetalleCredito> detalles = new ArrayList<>();
    private List<Abono> abonos = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public int getLocatarioId() { return locatarioId; }
    public void setLocatarioId(int locatarioId) { this.locatarioId = locatarioId; }
    public String getLocatarioNombre() { return locatarioNombre; }
    public void setLocatarioNombre(String locatarioNombre) { this.locatarioNombre = locatarioNombre; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<DetalleCredito> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCredito> detalles) { this.detalles = detalles; }
    public List<Abono> getAbonos() { return abonos; }
    public void setAbonos(List<Abono> abonos) { this.abonos = abonos; }
}
