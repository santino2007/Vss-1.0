package com.truco.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * El mazo de 40 cartas de la baraja española.
 * Se encarga de mezclar (barajar) y repartir las cartas de manera aleatoria.
 */
public class Mazo {

    private static final int[] NUMEROS = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12};

    private final Deque<Carta> cartas = new ArrayDeque<>();

    public Mazo() {
        reiniciar();
    }

    /** Vuelve a armar el mazo completo (40 cartas) y lo mezcla. */
    public void reiniciar() {
        List<Carta> todas = new ArrayList<>(40);
        for (Palo palo : Palo.values()) {
            for (int numero : NUMEROS) {
                todas.add(new Carta(numero, palo));
            }
        }
        Collections.shuffle(todas);
        cartas.clear();
        cartas.addAll(todas);
    }

    /** Reparte (saca) una carta de la punta del mazo. */
    public Carta repartirCarta() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío, no quedan cartas para repartir.");
        }
        return cartas.poll();
    }

    /** Reparte `cantidad` cartas. */
    public List<Carta> repartir(int cantidad) {
        List<Carta> mano = new ArrayList<>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            mano.add(repartirCarta());
        }
        return mano;
    }

    public int cartasRestantes() {
        return cartas.size();
    }
}
