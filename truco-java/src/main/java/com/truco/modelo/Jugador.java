package com.truco.modelo;

import com.truco.excepciones.JugadaInvalidaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Representa a un jugador de la partida: su nombre, la mano de cartas que
 * tiene en un momento dado, el puntaje acumulado y las manos (bazas) ganadas
 * dentro de la mano actual.
 */
public class Jugador {

    private final String nombre;
    private final List<Carta> mano = new ArrayList<>();
    // Las 3 cartas repartidas en la ronda. No cambia al jugar cartas: el envido y la
    // flor se calculan siempre sobre las cartas originales, no sobre las que quedan en mano.
    private final List<Carta> manoInicial = new ArrayList<>();
    private int puntos = 0;
    private int bazasGanadas = 0;

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Carta> getMano() {
        return mano;
    }

    public void recibirCartas(List<Carta> cartas) {
        mano.clear();
        mano.addAll(cartas);
        manoInicial.clear();
        manoInicial.addAll(cartas);
    }

    /** Las cartas que se repartieron al comienzo de la ronda (aunque ya se hayan jugado). */
    public List<Carta> getManoInicial() {
        return Collections.unmodifiableList(manoInicial);
    }

    /**
     * Tantos de envido: si hay dos (o tres) cartas del mismo palo, valen 20 + la suma de las
     * dos más altas (las figuras 10, 11 y 12 valen 0). Si no hay dos cartas del mismo palo,
     * los tantos son el valor de la carta más alta.
     */
    public int tantosEnvido() {
        int mejor = 0;
        for (Palo palo : Palo.values()) {
            List<Integer> valores = manoInicial.stream()
                    .filter(c -> c.getPalo() == palo)
                    .map(Carta::valorEnvido)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            if (valores.size() >= 2) {
                mejor = Math.max(mejor, 20 + valores.get(0) + valores.get(1));
            } else if (valores.size() == 1) {
                mejor = Math.max(mejor, valores.get(0));
            }
        }
        return mejor;
    }

    /** Hay flor cuando las 3 cartas repartidas son del mismo palo. */
    public boolean tieneFlor() {
        return manoInicial.size() == 3
                && manoInicial.stream().map(Carta::getPalo).distinct().count() == 1;
    }

    /** Tantos de la flor: 20 + la suma de las 3 cartas (las figuras valen 0). */
    public int tantosFlor() {
        if (!tieneFlor()) {
            throw new JugadaInvalidaException(nombre + " no tiene flor.");
        }
        return 20 + manoInicial.stream().mapToInt(Carta::valorEnvido).sum();
    }

    /** Juega (y retira de la mano) la carta ubicada en la posición indicada (0,1,2). */
    public Carta jugarCarta(int indice) {
        if (indice < 0 || indice >= mano.size()) {
            throw new JugadaInvalidaException(
                    "Índice de carta inválido: " + indice + ". " + nombre + " tiene " + mano.size() + " carta(s) disponible(s).");
        }
        return mano.remove(indice);
    }

    public boolean tieneCartas() {
        return !mano.isEmpty();
    }

    public int getPuntos() {
        return puntos;
    }

    public void sumarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new JugadaInvalidaException("No se pueden sumar puntos negativos.");
        }
        this.puntos += cantidad;
    }

    public int getBazasGanadas() {
        return bazasGanadas;
    }

    public void sumarBazaGanada() {
        this.bazasGanadas++;
    }

    public void reiniciarBazas() {
        this.bazasGanadas = 0;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
