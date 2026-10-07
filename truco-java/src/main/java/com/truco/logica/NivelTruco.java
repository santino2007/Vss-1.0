package com.truco.logica;

/**
 * Representa el estado del "sistema de Truco" (Truco / Retruco / Vale cuatro)
 * dentro de una ronda, junto con los puntos que se juegan en cada nivel.
 */
public enum NivelTruco {
    SIN_CANTAR(1),   // Si nadie canta truco, la mano/ronda vale 1 punto.
    TRUCO(2),
    RETRUCO(3),
    VALE_CUATRO(4);

    private final int puntos;

    NivelTruco(int puntos) {
        this.puntos = puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    /** Siguiente nivel posible de escalada (o null si ya es el máximo). */
    public NivelTruco siguienteNivel() {
        return switch (this) {
            case SIN_CANTAR -> TRUCO;
            case TRUCO -> RETRUCO;
            case RETRUCO -> VALE_CUATRO;
            case VALE_CUATRO -> null;
        };
    }

    public String nombreCanto() {
        return switch (this) {
            case SIN_CANTAR -> "-";
            case TRUCO -> "¡Truco!";
            case RETRUCO -> "¡Retruco!";
            case VALE_CUATRO -> "¡Vale cuatro!";
        };
    }
}
