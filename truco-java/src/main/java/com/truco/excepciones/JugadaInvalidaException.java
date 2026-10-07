package com.truco.excepciones;

/**
 * Excepción no chequeada (unchecked) que se lanza cuando un jugador intenta
 * realizar una jugada inválida: jugar una carta que no tiene, cantar un
 * truco fuera de turno, responder a un canto que no está pendiente, etc.
 */
public class JugadaInvalidaException extends RuntimeException {

    public JugadaInvalidaException(String mensaje) {
        super(mensaje);
    }

    public JugadaInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
