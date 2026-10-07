package com.truco.logica;

import com.truco.excepciones.JugadaInvalidaException;
import com.truco.modelo.Jugador;
import com.truco.modelo.Mazo;

/**
 * Representa la partida completa entre 2 jugadores: administra el mazo,
 * el puntaje, quién reparte / es "mano" en cada ronda, y la creación de
 * nuevas Rondas hasta que alguno de los dos jugadores alcanza el puntaje
 * objetivo (15 o 30, "malas" y "buenas").
 */
public class Partida {

    private final Jugador jugador1;
    private final Jugador jugador2;
    private final Mazo mazo = new Mazo();
    private final int puntosParaGanar;

    private Ronda rondaActual;
    private Jugador jugadorMano; // quien reparte/empieza la ronda actual
    private boolean partidaTerminada = false;
    private Jugador ganadorPartida = null;

    public Partida(String nombreJugador1, String nombreJugador2, int puntosParaGanar) {
        if (puntosParaGanar != 15 && puntosParaGanar != 30) {
            throw new JugadaInvalidaException("La partida debe jugarse a 15 o 30 puntos.");
        }
        this.jugador1 = new Jugador(nombreJugador1);
        this.jugador2 = new Jugador(nombreJugador2);
        this.puntosParaGanar = puntosParaGanar;
        // Sorteamos al azar quién empieza siendo "mano".
        this.jugadorMano = Math.random() < 0.5 ? jugador1 : jugador2;
        iniciarNuevaRonda();
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public int getPuntosParaGanar() {
        return puntosParaGanar;
    }

    public Ronda getRondaActual() {
        return rondaActual;
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }

    public Jugador getGanadorPartida() {
        return ganadorPartida;
    }

    public Jugador oponenteDe(Jugador jugador) {
        return jugador == jugador1 ? jugador2 : jugador1;
    }

    /** Arranca una nueva ronda: si el mazo se queda sin cartas suficientes, se rearma y mezcla de nuevo. */
    public void iniciarNuevaRonda() {
        if (mazo.cartasRestantes() < 6) {
            mazo.reiniciar();
        }
        rondaActual = new Ronda(jugador1, jugador2, jugadorMano, mazo, puntosParaGanar);
    }

    /**
     * Debe llamarse una vez que `rondaActual.isRondaTerminada()` es true.
     * Actualiza el puntaje, verifica si hay un ganador de la partida y,
     * si no lo hay, prepara la siguiente ronda (rotando quién es "mano").
     */
    public void cerrarRondaYContinuar() {
        if (!rondaActual.isRondaTerminada()) {
            throw new JugadaInvalidaException("La ronda actual todavía no terminó.");
        }
        if (jugador1.getPuntos() >= puntosParaGanar || jugador2.getPuntos() >= puntosParaGanar) {
            partidaTerminada = true;
            ganadorPartida = jugador1.getPuntos() >= puntosParaGanar ? jugador1 : jugador2;
            return;
        }
        // Rota el que reparte / es mano.
        jugadorMano = oponenteDe(jugadorMano);
        iniciarNuevaRonda();
    }

    public String estadoActual() {
        return String.format("%s: %d pts | %s: %d pts | Jugando a %d puntos | Mano: %s",
                jugador1.getNombre(), jugador1.getPuntos(),
                jugador2.getNombre(), jugador2.getPuntos(),
                puntosParaGanar, jugadorMano.getNombre());
    }
}
