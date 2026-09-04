package com.silver.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para construir el flujo de bytes ESC/POS que entienden la
 * mayoria de las impresoras termicas de tickets (58mm y 80mm, USB).
 * No depende de librerias externas: solo arma los comandos crudos.
 */
public class EscPosBuilder {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;

    public EscPosBuilder init() {
        write(ESC, '@'); // Reset de la impresora
        return this;
    }

    public EscPosBuilder alinearCentro() {
        write(ESC, 'a', 1);
        return this;
    }

    public EscPosBuilder alinearIzquierda() {
        write(ESC, 'a', 0);
        return this;
    }

    public EscPosBuilder negritaOn() {
        write(ESC, 'E', 1);
        return this;
    }

    public EscPosBuilder negritaOff() {
        write(ESC, 'E', 0);
        return this;
    }

    public EscPosBuilder textoGrandeOn() {
        write(GS, '!', 0x11); // doble alto y ancho
        return this;
    }

    public EscPosBuilder textoGrandeOff() {
        write(GS, '!', 0x00);
        return this;
    }

    public EscPosBuilder texto(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buffer.writeBytes(bytes);
        return this;
    }

    public EscPosBuilder linea(String s) {
        texto(s);
        salto();
        return this;
    }

    public EscPosBuilder salto() {
        buffer.write('\n');
        return this;
    }

    public EscPosBuilder separador(int ancho) {
        linea("-".repeat(ancho));
        return this;
    }

    public EscPosBuilder cortar() {
        salto(); salto(); salto();
        write(GS, 'V', 1); // corte parcial
        return this;
    }

    public byte[] build() {
        return buffer.toByteArray();
    }

    private void write(int... bytesAsInt) {
        for (int b : bytesAsInt) {
            buffer.write(b);
        }
    }
}
