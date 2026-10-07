package com.truco.modelo;

import java.util.Objects;

/**
 * Representa un naipe de la baraja española (1 al 7, 10, 11 y 12).
 * Conoce su propia jerarquía dentro del Truco argentino, según el
 * reglamento tradicional:
 *
 *  14 - 1 de Espada  (Ancho de espada)
 *  13 - 1 de Basto   (Ancho de basto)
 *  12 - 7 de Espada
 *  11 - 7 de Oro
 *  10 - 3 (de cualquier palo)
 *   9 - 2 (de cualquier palo)
 *   8 - 1 de Oro, 1 de Copa, 12, 11 y 10 (de cualquier palo) -> "cartas bajas/figuras"
 *   7 - 7 de Basto, 7 de Copa
 *   6 - 6 (de cualquier palo)
 *   5 - 5 (de cualquier palo)
 *   4 - 4 (de cualquier palo)
 */
public class Carta {

    private final int numero; // 1,2,3,4,5,6,7,10,11,12
    private final Palo palo;

    public Carta(int numero, Palo palo) {
        if (numero < 1 || numero > 12 || numero == 8 || numero == 9) {
            throw new IllegalArgumentException("Número de carta inválido para la baraja española: " + numero);
        }
        this.numero = numero;
        this.palo = palo;
    }

    public int getNumero() {
        return numero;
    }

    public Palo getPalo() {
        return palo;
    }

    /**
     * Devuelve la jerarquía de la carta para el Truco (a mayor valor, más fuerte).
     */
    public int jerarquiaTruco() {
        if (numero == 1 && palo == Palo.ESPADA) return 14;
        if (numero == 1 && palo == Palo.BASTO) return 13;
        if (numero == 7 && palo == Palo.ESPADA) return 12;
        if (numero == 7 && palo == Palo.ORO) return 11;
        if (numero == 3) return 10;
        if (numero == 2) return 9;
        if (numero == 1 || numero == 12 || numero == 11 || numero == 10) return 8;
        if (numero == 7) return 7; // 7 de basto o 7 de copa
        if (numero == 6) return 6;
        if (numero == 5) return 5;
        if (numero == 4) return 4;
        throw new IllegalStateException("Carta con jerarquía indefinida: " + this);
    }

    /**
     * Valor de la carta para el Envido y la Flor: las figuras (10, 11 y 12)
     * valen 0 y el resto vale su número.
     */
    public int valorEnvido() {
        if (numero == 10 || numero == 11 || numero == 12) return 0;
        return numero;
    }

    @Override
    public String toString() {
        String nombreNumero = switch (numero) {
            case 1 -> "As";
            case 10 -> "Sota";
            case 11 -> "Caballo";
            case 12 -> "Rey";
            default -> String.valueOf(numero);
        };
        return nombreNumero + " de " + palo.getNombre() + " " + palo.getSimbolo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carta carta)) return false;
        return numero == carta.numero && palo == carta.palo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, palo);
    }
}
