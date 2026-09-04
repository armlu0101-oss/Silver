package com.silver.service;

import com.silver.model.DetalleVenta;
import com.silver.model.Venta;
import com.silver.util.EscPosBuilder;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Genera el ticket de venta en formato ESC/POS y lo envia a una impresora
 * termica local (USB, 58mm o 80mm) usando la API estandar javax.print.
 * El sistema funciona sin internet: la impresora se detecta como
 * dispositivo local del sistema operativo.
 */
public class TicketService {

    private static final String NOMBRE_NEGOCIO = "SILVER CALZADO";

    /** Ancho de columnas segun el papel: 32 para 58mm, 48 para 80mm. */
    public byte[] generarTicket(Venta venta, int anchoColumnas) {
        EscPosBuilder t = new EscPosBuilder().init();

        t.alinearCentro()
         .textoGrandeOn()
         .linea(NOMBRE_NEGOCIO)
         .textoGrandeOff()
         .linea("Botas y Calzado")
         .separador(anchoColumnas)
         .alinearIzquierda();

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("es", "MX"));
        t.linea("Fecha: " + fmt.format(venta.getFecha() != null ? venta.getFecha() : new java.util.Date()));
        t.linea("Venta No.: " + venta.getId());
        t.linea("Atendio: " + venta.getUsuarioNombre());
        t.separador(anchoColumnas);

        t.negritaOn();
        t.linea(pad("Producto", "Cant", "Precio", "Subt.", anchoColumnas));
        t.negritaOff();

        for (DetalleVenta d : venta.getDetalles()) {
            t.linea(pad(recortar(d.getProductoNombre(), 18), String.valueOf(d.getCantidad()),
                    "$" + d.getPrecioUnitario(), "$" + d.getSubtotal(), anchoColumnas));
        }

        t.separador(anchoColumnas);
        t.negritaOn();
        t.linea("TOTAL:                $" + venta.getTotal());
        t.negritaOff();
        t.salto();
        t.alinearCentro();
        t.linea("Gracias por su compra");
        t.linea("SILVER 2024");
        t.cortar();

        return t.build();
    }

    /** Envia los bytes ESC/POS a la impresora indicada (o a la impresora predeterminada si nombreImpresora es null). */
    public void imprimir(byte[] datosTicket, String nombreImpresora) throws PrintException {
        PrintService servicio = obtenerServicioImpresion(nombreImpresora);
        if (servicio == null) {
            throw new PrintException("No se encontro una impresora termica disponible. " +
                    "Verifique que este conectada por USB y configurada en el sistema operativo.");
        }

        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(datosTicket, flavor, null);
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();

        DocPrintJob job = servicio.createPrintJob();
        job.print(doc, attrs);
    }

    public PrintService[] listarImpresoras() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }

    private PrintService obtenerServicioImpresion(String nombre) {
        PrintService[] servicios = listarImpresoras();
        if (nombre == null || nombre.isBlank()) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }
        for (PrintService s : servicios) {
            if (s.getName().equalsIgnoreCase(nombre)) return s;
        }
        return null;
    }

    private String pad(String c1, String c2, String c3, String c4, int ancho) {
        int wCant = 5, wPrecio = ancho >= 40 ? 10 : 8;
        int wProd = ancho - wCant - wPrecio - 8;
        return String.format("%-" + wProd + "s%" + wCant + "s%" + wPrecio + "s", c1, c2, c3) + " " + c4;
    }

    private String recortar(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }
}
