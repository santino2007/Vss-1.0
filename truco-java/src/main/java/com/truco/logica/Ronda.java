package com.truco.logica;

import com.truco.excepciones.JugadaInvalidaException;
import com.truco.modelo.Carta;
import com.truco.modelo.Jugador;
import com.truco.modelo.Mazo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Una Ronda representa el reparto de 3 cartas a cada jugador y el juego de
 * hasta 3 "manos" (bazas), siguiendo la terminología del enunciado:
 *   - "mano": cada enfrentamiento de una carta contra otra (una baza).
 *   - "ronda": el conjunto de hasta 3 manos que define quién se lleva los
 *              puntos que estén en juego (según el nivel de Truco cantado).
 *
 * También administra el canto de Truco / Retruco / Vale cuatro y las
 * respuestas de Quiero / No quiero, y los "tantos": el Envido (y sus
 * subidas) y la Flor.
 *
 * Reglas de los tantos implementadas:
 *  - Solo se pueden cantar durante la primera mano, mientras no se haya aceptado el Truco
 *    (el envido "está primero": el que recibe un Truco puede contestar con Envido o Flor).
 *  - El envido se juega una sola vez por ronda; gana quien tenga más tantos y, si empatan,
 *    gana el jugador "mano".
 *  - Si alguien tiene Flor, la flor anula el envido: se la lleva quien la tenga (3 puntos);
 *    si los dos tienen flor gana la más alta (o el mano si empatan) y se lleva 6 puntos.
 */
public class Ronda {

    /** Puntos que vale una flor (si los dos jugadores tienen flor, el ganador se lleva el doble). */
    public static final int PUNTOS_FLOR = 3;

    /** Resultado de una mano (baza): el jugador ganador, o null si fue parda (empate). */
    private final List<Jugador> resultadosManos = new ArrayList<>();
    private final List<Carta[]> cartasJugadasPorMano = new ArrayList<>();

    // Carta que cada jugador ya puso sobre la mesa en la mano (baza) en curso.
    // Índice 0 = jugador1, índice 1 = jugador2. null = todavía no jugó.
    private final Carta[] cartasEnMesa = new Carta[2];

    private final Jugador jugador1;
    private final Jugador jugador2;
    private final Jugador jugadorMano; // quien empieza jugando esta ronda

    private Jugador turnoActual;

    private NivelTruco nivelTruco = NivelTruco.SIN_CANTAR;
    private Jugador ultimoQueCanto = null;
    private boolean respuestaPendiente = false;

    private final int puntosParaGanar; // se necesita para calcular la Falta Envido y cortar la ronda al llegar al objetivo
    private boolean trucoAceptado = false;

    // Envido
    private final List<TipoEnvido> cantosEnvido = new ArrayList<>();
    private Jugador ultimoQueCantoEnvido = null;
    private boolean envidoPendiente = false;
    private boolean envidoResuelto = false; // jugado, rechazado o anulado por una flor

    // Flor
    private boolean florResuelta = false;

    // Texto con el resultado de los tantos, para que la interfaz lo muestre una sola vez.
    private String anuncioTantos = null;

    private Jugador ganadorRonda = null;
    private boolean rondaCortadaPorPuntos = false; // alguien llegó al puntaje de la partida con envido/flor
    private boolean rondaTerminada = false;
    private boolean alguienSeFueAlMazo = false;
    private Jugador jugadorSeFueAlMazo = null;

    public Ronda(Jugador jugador1, Jugador jugador2, Jugador jugadorMano, Mazo mazo, int puntosParaGanar) {
        this.puntosParaGanar = puntosParaGanar;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.jugadorMano = jugadorMano;
        this.turnoActual = jugadorMano;

        jugador1.reiniciarBazas();
        jugador2.reiniciarBazas();

        jugador1.recibirCartas(mazo.repartir(3));
        jugador2.recibirCartas(mazo.repartir(3));
    }

    public Jugador getJugadorMano() {
        return jugadorMano;
    }

    public Jugador getTurnoActual() {
        return turnoActual;
    }

    public NivelTruco getNivelTruco() {
        return nivelTruco;
    }

    public boolean isRespuestaPendiente() {
        return respuestaPendiente;
    }

    public boolean isRondaTerminada() {
        return rondaTerminada;
    }

    public Jugador getGanadorRonda() {
        return ganadorRonda;
    }

    public int getNumeroManoActual() {
        return resultadosManos.size(); // 0,1,2
    }

    public List<Carta[]> getCartasJugadasPorMano() {
        return cartasJugadasPorMano;
    }

    /** Carta que el jugador ya puso en la mesa en la baza en curso (o null si todavía no jugó). */
    public Carta getCartaEnMesa(Jugador jugador) {
        return cartasEnMesa[jugador == jugador1 ? 0 : 1];
    }

    private Jugador oponenteDe(Jugador jugador) {
        return jugador == jugador1 ? jugador2 : jugador1;
    }

    // ---------------------------------------------------------------
    // Juego de cartas (manos / bazas)
    // ---------------------------------------------------------------

    /**
     * El jugador de turno juega la carta ubicada en `indiceCarta` de su mano.
     * Cuando ambos jugadores jugaron su carta para la mano actual, se resuelve
     * el ganador de esa mano y se actualiza el turno para la siguiente.
     */
    public void jugarCarta(Jugador jugador, int indiceCarta) {
        if (rondaTerminada) {
            throw new JugadaInvalidaException("La ronda ya terminó, no se pueden jugar más cartas.");
        }
        if (envidoPendiente) {
            throw new JugadaInvalidaException("Hay un Envido pendiente de respuesta (Quiero / No quiero).");
        }
        if (respuestaPendiente) {
            throw new JugadaInvalidaException("Hay un canto de Truco pendiente de respuesta (Quiero / No quiero).");
        }
        if (jugador != turnoActual) {
            throw new JugadaInvalidaException("No es el turno de " + jugador.getNombre() + ".");
        }

        Carta carta = jugador.jugarCarta(indiceCarta);
        int indice = (jugador == jugador1) ? 0 : 1;
        cartasEnMesa[indice] = carta;

        int otroIndice = 1 - indice;
        if (cartasEnMesa[otroIndice] != null) {
            // Ambos ya jugaron su carta para esta mano: se resuelve.
            Carta[] jugadas = new Carta[]{cartasEnMesa[0], cartasEnMesa[1]};
            cartasEnMesa[0] = null;
            cartasEnMesa[1] = null;
            resolverMano(jugadas);
        } else {
            // Falta que el rival juegue su carta para esta mano.
            turnoActual = oponenteDe(jugador);
        }
    }

    private void resolverMano(Carta[] jugadas) {
        Carta cartaJ1 = jugadas[0];
        Carta cartaJ2 = jugadas[1];
        cartasJugadasPorMano.add(jugadas);

        Jugador ganadorMano;
        if (cartaJ1.jerarquiaTruco() > cartaJ2.jerarquiaTruco()) {
            ganadorMano = jugador1;
        } else if (cartaJ2.jerarquiaTruco() > cartaJ1.jerarquiaTruco()) {
            ganadorMano = jugador2;
        } else {
            ganadorMano = null; // parda
        }
        if (ganadorMano != null) {
            ganadorMano.sumarBazaGanada();
        }
        resultadosManos.add(ganadorMano);

        Jugador ganador = evaluarGanadorRonda();
        if (ganador != null) {
            finalizarRonda(ganador);
        } else {
            // Definir quién empieza la siguiente mano: el ganador de esta mano,
            // o si fue parda, sigue el mismo jugador que era "mano".
            turnoActual = (ganadorMano != null) ? ganadorMano : jugadorMano;
        }
    }

    /**
     * Aplica las reglas clásicas de "parda" (empate) del Truco para determinar
     * si la ronda ya tiene un ganador con las manos jugadas hasta el momento.
     * Devuelve null si todavía hace falta jugar más manos.
     */
    private Jugador evaluarGanadorRonda() {
        int jugadas = resultadosManos.size();
        if (jugadas == 1) {
            return null; // nunca se define con una sola mano
        }
        Jugador r0 = resultadosManos.get(0);
        Jugador r1 = resultadosManos.get(1);
        if (jugadas == 2) {
            if (r0 == null && r1 != null) return r1;            // parda + ganador -> gana el de la 2da
            if (r0 == null && r1 == null) return null;          // dos pardas -> se define en la 3ra
            if (r0 != null && r1 == null) return r0;             // ganador + parda -> gana el de la 1ra
            if (r0 == r1) return r0;                             // mismo ganador en ambas -> gana la ronda
            return null;                                         // ganadores distintos -> se define en la 3ra
        }
        // jugadas == 3
        Jugador r2 = resultadosManos.get(2);
        if (r0 == null && r1 == null) {
            // las dos primeras fueron pardas: decide la 3ra, y si también es parda, gana el jugador "mano"
            return (r2 != null) ? r2 : jugadorMano;
        }
        // r0 y r1 tuvieron ganadores distintos: decide la 3ra, y si es parda gana quien ganó la 1ra
        return (r2 != null) ? r2 : r0;
    }

    private void finalizarRonda(Jugador ganador) {
        this.ganadorRonda = ganador;
        this.rondaTerminada = true;
        ganador.sumarPuntos(nivelTruco.getPuntos());
    }

    // ---------------------------------------------------------------
    // Sistema de Truco: cantar / responder
    // ---------------------------------------------------------------

    /** El jugador indicado canta Truco, Retruco o Vale cuatro (según corresponda). */
    public void cantarTruco(Jugador jugador) {
        if (rondaTerminada) {
            throw new JugadaInvalidaException("La ronda ya terminó.");
        }
        if (respuestaPendiente || envidoPendiente) {
            throw new JugadaInvalidaException("Ya hay un canto esperando respuesta.");
        }
        if (nivelTruco != NivelTruco.SIN_CANTAR && ultimoQueCanto == jugador) {
            throw new JugadaInvalidaException("No podés volver a cantar, le toca responder al rival.");
        }
        NivelTruco siguiente = nivelTruco.siguienteNivel();
        if (siguiente == null) {
            throw new JugadaInvalidaException("Ya se cantó Vale Cuatro, no se puede escalar más.");
        }
        nivelTruco = siguiente;
        ultimoQueCanto = jugador;
        respuestaPendiente = true;
    }

    /** El rival del que cantó responde "Quiero": el juego continúa por los puntos del nivel actual. */
    public void quiero() {
        if (envidoPendiente) {
            throw new JugadaInvalidaException("Primero hay que responder el Envido.");
        }
        if (!respuestaPendiente) {
            throw new JugadaInvalidaException("No hay ningún canto de Truco pendiente de respuesta.");
        }
        respuestaPendiente = false;
        trucoAceptado = true; // a partir de acá ya no se puede cantar envido ni flor
        // El turno para seguir jugando cartas vuelve a quien correspondía antes del canto.
    }

    /**
     * El rival del que cantó responde "No quiero": la ronda termina inmediatamente
     * y los puntos del nivel ANTERIOR al cantado quedan para quien cantó.
     */
    public void noQuiero() {
        if (envidoPendiente) {
            throw new JugadaInvalidaException("Primero hay que responder el Envido.");
        }
        if (!respuestaPendiente) {
            throw new JugadaInvalidaException("No hay ningún canto de Truco pendiente de respuesta.");
        }
        NivelTruco nivelAnterior = switch (nivelTruco) {
            case TRUCO -> NivelTruco.SIN_CANTAR;
            case RETRUCO -> NivelTruco.TRUCO;
            case VALE_CUATRO -> NivelTruco.RETRUCO;
            case SIN_CANTAR -> NivelTruco.SIN_CANTAR;
        };
        respuestaPendiente = false;
        rondaTerminada = true;
        ganadorRonda = ultimoQueCanto;
        ultimoQueCanto.sumarPuntos(nivelAnterior.getPuntos());
    }

    /** El jugador indicado se va al mazo: pierde la ronda y el rival se lleva los puntos actuales en juego. */
    public void irseAlMazo(Jugador jugador) {
        if (rondaTerminada) {
            throw new JugadaInvalidaException("La ronda ya terminó.");
        }
        if (envidoPendiente) {
            throw new JugadaInvalidaException("Primero hay que responder el Envido.");
        }
        alguienSeFueAlMazo = true;
        jugadorSeFueAlMazo = jugador;
        rondaTerminada = true;
        Jugador rival = oponenteDe(jugador);
        ganadorRonda = rival;
        respuestaPendiente = false;
        rival.sumarPuntos(nivelTruco.getPuntos());
    }


    // ---------------------------------------------------------------
    // Tantos: Envido y Flor
    // ---------------------------------------------------------------

    /** Cuándo se puede arrancar un canto de tantos (Envido o Flor): lo comparten ambos. */
    private boolean habilitadoParaTantos(Jugador jugador) {
        if (rondaTerminada || envidoPendiente || envidoResuelto || florResuelta) return false;
        if (getNumeroManoActual() != 0 || trucoAceptado) return false; // solo en la 1ra mano y antes de aceptar el truco
        if (respuestaPendiente) return jugador != ultimoQueCanto;      // "el envido está primero": responde al truco
        return jugador == turnoActual;
    }

    public boolean puedeCantarEnvido(Jugador jugador) {
        return habilitadoParaTantos(jugador);
    }

    public boolean puedeCantarFlor(Jugador jugador) {
        return habilitadoParaTantos(jugador) && jugador.tieneFlor();
    }

    /**
     * El jugador canta Envido / Real Envido / Falta Envido. Si ya hay un envido esperando
     * respuesta, el rival del que cantó puede "subir" con un canto mayor.
     * Si alguno de los dos tiene flor, la flor anula el envido y se resuelve directamente.
     */
    public void cantarEnvido(Jugador jugador, TipoEnvido tipo) {
        if (rondaTerminada) {
            throw new JugadaInvalidaException("La ronda ya terminó.");
        }
        if (envidoPendiente) {
            if (jugador == ultimoQueCantoEnvido) {
                throw new JugadaInvalidaException("No podés subir tu propio canto, le toca responder al rival.");
            }
            if (!tiposEnvidoParaSubir().contains(tipo)) {
                throw new JugadaInvalidaException("No se puede cantar " + tipo.getEtiqueta() + " después de ese canto.");
            }
            cantosEnvido.add(tipo);
            ultimoQueCantoEnvido = jugador;
            return;
        }
        if (!puedeCantarEnvido(jugador)) {
            throw new JugadaInvalidaException(
                    "El envido solo se puede cantar una vez, en la primera mano y antes de aceptar el truco.");
        }
        if (jugador1.tieneFlor() || jugador2.tieneFlor()) {
            resolverFlor(jugador, true);
            return;
        }
        cantosEnvido.add(tipo);
        ultimoQueCantoEnvido = jugador;
        envidoPendiente = true;
    }

    /** Cantos con los que se puede subir el envido pendiente (vacío si no hay envido pendiente). */
    public List<TipoEnvido> tiposEnvidoParaSubir() {
        List<TipoEnvido> posibles = new ArrayList<>();
        if (!envidoPendiente) {
            return posibles;
        }
        switch (cantosEnvido.get(cantosEnvido.size() - 1)) {
            case ENVIDO -> {
                long envidos = cantosEnvido.stream().filter(t -> t == TipoEnvido.ENVIDO).count();
                if (envidos < 2) posibles.add(TipoEnvido.ENVIDO); // "Envido, Envido" como máximo
                posibles.add(TipoEnvido.REAL_ENVIDO);
                posibles.add(TipoEnvido.FALTA_ENVIDO);
            }
            case REAL_ENVIDO -> posibles.add(TipoEnvido.FALTA_ENVIDO);
            case FALTA_ENVIDO -> { /* no se puede subir más */ }
        }
        return posibles;
    }

    /** El rival del que cantó acepta el envido: se comparan los tantos y el ganador suma los puntos. */
    public void quieroEnvido() {
        if (!envidoPendiente) {
            throw new JugadaInvalidaException("No hay ningún envido pendiente de respuesta.");
        }
        int tantos1 = jugador1.tantosEnvido();
        int tantos2 = jugador2.tantosEnvido();
        Jugador ganador = tantos1 > tantos2 ? jugador1 : (tantos2 > tantos1 ? jugador2 : jugadorMano);
        int puntos = getPuntosEnvidoEnJuego();

        envidoPendiente = false;
        envidoResuelto = true;
        ganador.sumarPuntos(puntos);
        anuncioTantos = jugador1.getNombre() + ": " + tantos1 + " tantos\n"
                + jugador2.getNombre() + ": " + tantos2 + " tantos\n"
                + "Gana el envido " + ganador.getNombre()
                + (tantos1 == tantos2 ? " (empate: gana el mano)" : "")
                + " y suma " + puntos + " punto(s).";
        verificarFinPorPuntos(ganador);
    }

    /**
     * El rival del que cantó rechaza el envido: el que cantó suma los puntos de lo que ya estaba
     * aceptado (1 si fue un solo canto).
     */
    public void noQuieroEnvido() {
        if (!envidoPendiente) {
            throw new JugadaInvalidaException("No hay ningún envido pendiente de respuesta.");
        }
        int aceptado = 0;
        for (int i = 0; i < cantosEnvido.size() - 1; i++) {
            aceptado += cantosEnvido.get(i).getPuntos();
        }
        int puntos = Math.max(1, aceptado);
        Jugador ganador = ultimoQueCantoEnvido;

        envidoPendiente = false;
        envidoResuelto = true;
        ganador.sumarPuntos(puntos);
        anuncioTantos = oponenteDe(ganador).getNombre() + " no quiso el envido.\n"
                + ganador.getNombre() + " suma " + puntos + " punto(s).";
        verificarFinPorPuntos(ganador);
    }

    /** El jugador canta Flor (necesita tener las 3 cartas del mismo palo). */
    public void cantarFlor(Jugador jugador) {
        if (rondaTerminada) {
            throw new JugadaInvalidaException("La ronda ya terminó.");
        }
        if (!jugador.tieneFlor()) {
            throw new JugadaInvalidaException(jugador.getNombre() + " no tiene flor (hacen falta 3 cartas del mismo palo).");
        }
        if (!puedeCantarFlor(jugador)) {
            throw new JugadaInvalidaException(
                    "La flor solo se puede cantar una vez, en la primera mano y antes de aceptar el truco.");
        }
        resolverFlor(jugador, false);
    }

    private void resolverFlor(Jugador quienCanto, boolean anulaEnvido) {
        boolean flor1 = jugador1.tieneFlor();
        boolean flor2 = jugador2.tieneFlor();
        Jugador ganador;
        int puntos;
        String detalle;
        if (flor1 && flor2) {
            int tantos1 = jugador1.tantosFlor();
            int tantos2 = jugador2.tantosFlor();
            ganador = tantos1 > tantos2 ? jugador1 : (tantos2 > tantos1 ? jugador2 : jugadorMano);
            puntos = PUNTOS_FLOR * 2;
            detalle = "¡Flor de los dos!\n"
                    + jugador1.getNombre() + ": " + tantos1 + " tantos\n"
                    + jugador2.getNombre() + ": " + tantos2 + " tantos\n"
                    + "Gana la flor " + ganador.getNombre()
                    + (tantos1 == tantos2 ? " (empate: gana el mano)" : "")
                    + " y suma " + puntos + " puntos.";
        } else {
            ganador = flor1 ? jugador1 : jugador2;
            puntos = PUNTOS_FLOR;
            detalle = "¡Flor de " + ganador.getNombre() + "! (" + ganador.tantosFlor() + " tantos)\n"
                    + oponenteDe(ganador).getNombre() + " no tiene flor.\n"
                    + ganador.getNombre() + " suma " + puntos + " puntos.";
        }
        if (anulaEnvido) {
            detalle = quienCanto.getNombre() + " cantó envido, pero hay flor: la flor anula el envido.\n" + detalle;
        }

        florResuelta = true;
        envidoResuelto = true; // con flor en juego ya no se canta envido
        ganador.sumarPuntos(puntos);
        anuncioTantos = detalle;
        verificarFinPorPuntos(ganador);
    }

    /** Si el envido o la flor hicieron llegar a alguien al puntaje de la partida, la ronda termina ahí. */
    private void verificarFinPorPuntos(Jugador jugador) {
        if (jugador.getPuntos() >= puntosParaGanar) {
            rondaTerminada = true;
            rondaCortadaPorPuntos = true;
            ganadorRonda = jugador;
            respuestaPendiente = false;
            envidoPendiente = false;
        }
    }

    /**
     * Devuelve (una sola vez) el texto con el resultado del último envido/flor resuelto,
     * para mostrarlo en pantalla. Devuelve null si no hay nada nuevo para anunciar.
     */
    public String retirarAnuncioTantos() {
        String anuncio = anuncioTantos;
        anuncioTantos = null;
        return anuncio;
    }

    /** Puntos que valdría el envido si se acepta ahora (suma de los cantos, o la falta si hubo Falta Envido). */
    public int getPuntosEnvidoEnJuego() {
        if (cantosEnvido.contains(TipoEnvido.FALTA_ENVIDO)) {
            return Math.max(1, puntosParaGanar - Math.max(jugador1.getPuntos(), jugador2.getPuntos()));
        }
        return cantosEnvido.stream().mapToInt(TipoEnvido::getPuntos).sum();
    }

    public boolean isEnvidoPendiente() {
        return envidoPendiente;
    }

    public boolean isEnvidoResuelto() {
        return envidoResuelto;
    }

    public boolean isFlorResuelta() {
        return florResuelta;
    }

    public Jugador getUltimoQueCantoEnvido() {
        return ultimoQueCantoEnvido;
    }

    public List<TipoEnvido> getCantosEnvido() {
        return Collections.unmodifiableList(cantosEnvido);
    }

    public boolean isRondaCortadaPorPuntos() {
        return rondaCortadaPorPuntos;
    }

    /**
     * Quién tiene que actuar ahora: el que debe responder un envido, el que debe responder
     * un truco, o el jugador de turno (en ese orden de prioridad).
     */
    public Jugador getJugadorQueDebeActuar() {
        if (!rondaTerminada) {
            if (envidoPendiente) return oponenteDe(ultimoQueCantoEnvido);
            if (respuestaPendiente) return oponenteDe(ultimoQueCanto);
        }
        return turnoActual;
    }

    public boolean isAlguienSeFueAlMazo() {
        return alguienSeFueAlMazo;
    }

    public Jugador getJugadorSeFueAlMazo() {
        return jugadorSeFueAlMazo;
    }

    public Jugador getUltimoQueCanto() {
        return ultimoQueCanto;
    }
}