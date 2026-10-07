package com.truco.modelo;

/**
 * Los 4 palos de la baraja española utilizada en el Truco argentino.
 */
public enum Palo {
    ESPADA("Espada", "♠"),
    BASTO("Basto", "♣"),
    ORO("Oro", "♦"),
    COPA("Copa", "♥");

    private final String nombre;
    private final String simbolo;

    Palo(String nombre, String simbolo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
