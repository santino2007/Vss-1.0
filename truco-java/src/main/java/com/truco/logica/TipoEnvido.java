package com.truco.logica;

/**
 * Los cantos posibles del Envido. Se pueden encadenar (Envido, Envido, Real Envido,
 * Falta Envido...) y los puntos de la apuesta se suman, salvo la Falta Envido que
 * vale "lo que le falta al que va ganando para llegar al puntaje de la partida".
 */
public enum TipoEnvido {
    ENVIDO(2, "¡Envido!", "ENVIDO"),
    REAL_ENVIDO(3, "¡Real Envido!", "REAL ENVIDO"),
    FALTA_ENVIDO(0, "¡Falta Envido!", "FALTA ENVIDO"); // sus puntos se calculan en Ronda

    private final int puntos;
    private final String nombreCanto;
    private final String etiqueta;

    TipoEnvido(int puntos, String nombreCanto, String etiqueta) {
        this.puntos = puntos;
        this.nombreCanto = nombreCanto;
        this.etiqueta = etiqueta;
    }

    /** Puntos fijos del canto (0 para la Falta Envido, que depende del marcador). */
    public int getPuntos() {
        return puntos;
    }

    public boolean esFalta() {
        return this == FALTA_ENVIDO;
    }

    /** Texto para el registro de la partida, ej: "¡Real Envido!". */
    public String getNombreCanto() {
        return nombreCanto;
    }

    /** Texto corto para los botones, ej: "REAL ENVIDO". */
    public String getEtiqueta() {
        return etiqueta;
    }
}
