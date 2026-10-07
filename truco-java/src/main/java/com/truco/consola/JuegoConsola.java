package com.truco.consola;

import com.truco.excepciones.JugadaInvalidaException;
import com.truco.logica.NivelTruco;
import com.truco.logica.Partida;
import com.truco.logica.Ronda;
import com.truco.logica.TipoEnvido;
import com.truco.modelo.Carta;
import com.truco.modelo.Jugador;

import java.util.List;
import java.util.Scanner;

/**
 * Versión 100% por consola/terminal del juego de Truco.
 * Se puede ejecutar con: java -jar truco-argentino.jar consola
 */
public class JuegoConsola {

    private final Scanner sc = new Scanner(System.in);

    public void iniciar() {
        System.out.println("=======================================");
        System.out.println("   TRUCO ARGENTINO - Modo consola");
        System.out.println("=======================================");

        System.out.print("Nombre del Jugador 1: ");
        String nombre1 = leerNombre("Jugador 1");
        System.out.print("Nombre del Jugador 2: ");
        String nombre2 = leerNombre("Jugador 2");

        int puntos = leerPuntosObjetivo();

        Partida partida = new Partida(nombre1, nombre2, puntos);

        while (!partida.isPartidaTerminada()) {
            jugarRonda(partida);
            partida.cerrarRondaYContinuar();
        }

        System.out.println();
        System.out.println("*****************************************");
        System.out.println(" GANADOR DE LA PARTIDA: " + partida.getGanadorPartida().getNombre() + "!");
        System.out.println("*****************************************");
    }

    private String leerNombre(String porDefecto) {
        String linea = sc.nextLine().trim();
        return linea.isEmpty() ? porDefecto : linea;
    }

    private int leerPuntosObjetivo() {
        while (true) {
            System.out.print("¿A cuántos puntos jugamos, 15 o 30? ");
            String linea = sc.nextLine().trim();
            if (linea.equals("15") || linea.equals("30")) {
                return Integer.parseInt(linea);
            }
            System.out.println("Opción inválida. Ingresá 15 o 30.");
        }
    }

    private void jugarRonda(Partida partida) {
        Ronda ronda = partida.getRondaActual();
        System.out.println();
        System.out.println("----------- NUEVA RONDA -----------");
        System.out.println(partida.estadoActual());

        while (!ronda.isRondaTerminada()) {
            Jugador turno = ronda.getJugadorQueDebeActuar();
            mostrarEstadoMesa(ronda);
            mostrarMano(turno);

            if (ronda.isEnvidoPendiente()) {
                manejarRespuestaEnvido(ronda);
                anunciarTantos(ronda);
                continue;
            }
            if (ronda.isRespuestaPendiente()) {
                manejarRespuestaTruco(partida, ronda);
                continue;
            }

            System.out.println("Turno de " + turno.getNombre() + ". Opciones:");
            System.out.println("  1-" + turno.getMano().size() + ") Jugar carta");
            mostrarOpcionesTantos(ronda, turno);
            System.out.println("  t) Cantar Truco / Retruco / Vale cuatro");
            System.out.println("  m) Irse al mazo");
            System.out.print("Elegí una opción: ");
            String opcion = sc.nextLine().trim().toLowerCase();

            try {
                if (cantarTantos(ronda, turno, opcion)) {
                    anunciarTantos(ronda);
                } else if (opcion.equals("t")) {
                    ronda.cantarTruco(turno);
                    System.out.println(turno.getNombre() + " cantó " + ronda.getNivelTruco().nombreCanto());
                } else if (opcion.equals("m")) {
                    ronda.irseAlMazo(turno);
                    System.out.println(turno.getNombre() + " se fue al mazo.");
                } else {
                    int indice = Integer.parseInt(opcion) - 1;
                    Carta jugada = turno.getMano().get(indice);
                    ronda.jugarCarta(turno, indice);
                    System.out.println(turno.getNombre() + " jugó: " + jugada);
                }
            } catch (JugadaInvalidaException | NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println("¡Jugada inválida! " + e.getMessage());
            }
        }

        if (ronda.isAlguienSeFueAlMazo()) {
            System.out.println(ronda.getJugadorSeFueAlMazo().getNombre() + " se fue al mazo.");
        }
        if (ronda.isRondaCortadaPorPuntos()) {
            System.out.println(ronda.getGanadorRonda().getNombre() + " llegó al puntaje de la partida con los tantos.");
        }
        System.out.println("Ganador de la ronda: " + ronda.getGanadorRonda().getNombre());
        System.out.println(partida.estadoActual());
    }

    private void manejarRespuestaTruco(Partida partida, Ronda ronda) {
        Jugador rival = partida.oponenteDe(ronda.getUltimoQueCanto());
        System.out.println(ronda.getUltimoQueCanto().getNombre() + " cantó " + ronda.getNivelTruco().nombreCanto()
                + ". " + rival.getNombre() + ", ¿qué decís?");
        mostrarOpcionesTantos(ronda, rival);
        System.out.println("  q) Quiero");
        System.out.println("  n) No quiero");
        NivelTruco siguiente = ronda.getNivelTruco().siguienteNivel();
        if (siguiente != null) {
            System.out.println("  s) Quiero y subo a " + siguiente.nombreCanto());
        }
        System.out.print("Elegí una opción: ");
        String opcion = sc.nextLine().trim().toLowerCase();
        try {
            if (cantarTantos(ronda, rival, opcion)) {
                anunciarTantos(ronda);
                return;
            }
            switch (opcion) {
                case "q" -> ronda.quiero();
                case "n" -> ronda.noQuiero();
                case "s" -> {
                    if (siguiente == null) {
                        System.out.println("No se puede subir más.");
                    } else {
                        ronda.quiero();
                        ronda.cantarTruco(rival);
                    }
                }
                default -> System.out.println("Opción inválida.");
            }
        } catch (JugadaInvalidaException e) {
            System.out.println("¡Jugada inválida! " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Envido y Flor
    // ---------------------------------------------------------------

    /** Muestra las opciones de Flor / Envido que el jugador tiene disponibles en este momento. */
    private void mostrarOpcionesTantos(Ronda ronda, Jugador jugador) {
        if (ronda.puedeCantarFlor(jugador)) {
            System.out.println("  flor) Cantar Flor (tenés las 3 cartas del mismo palo)");
        }
        if (ronda.puedeCantarEnvido(jugador)) {
            System.out.println("  e) Envido   r) Real Envido   f) Falta Envido");
        }
    }

    /** Interpreta la opción como un canto de tantos. Devuelve true si lo era (y se ejecutó). */
    private boolean cantarTantos(Ronda ronda, Jugador jugador, String opcion) {
        switch (opcion) {
            case "flor" -> {
                ronda.cantarFlor(jugador);
                System.out.println(jugador.getNombre() + " cantó ¡Flor!");
            }
            case "e", "r", "f" -> {
                TipoEnvido tipo = switch (opcion) {
                    case "e" -> TipoEnvido.ENVIDO;
                    case "r" -> TipoEnvido.REAL_ENVIDO;
                    default -> TipoEnvido.FALTA_ENVIDO;
                };
                ronda.cantarEnvido(jugador, tipo);
                System.out.println(jugador.getNombre() + " cantó " + tipo.getNombreCanto());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void manejarRespuestaEnvido(Ronda ronda) {
        Jugador quienCanto = ronda.getUltimoQueCantoEnvido();
        Jugador quienResponde = ronda.getJugadorQueDebeActuar();
        System.out.println(quienCanto.getNombre() + " cantó " + ronda.getCantosEnvido().get(ronda.getCantosEnvido().size() - 1).getNombreCanto()
                + " (en juego: " + ronda.getPuntosEnvidoEnJuego() + " puntos). "
                + quienResponde.getNombre() + ", ¿qué decís?");
        System.out.println("  q) Quiero");
        System.out.println("  n) No quiero");
        List<TipoEnvido> subidas = ronda.tiposEnvidoParaSubir();
        if (subidas.contains(TipoEnvido.ENVIDO)) System.out.println("  e) Subir con Envido");
        if (subidas.contains(TipoEnvido.REAL_ENVIDO)) System.out.println("  r) Subir con Real Envido");
        if (subidas.contains(TipoEnvido.FALTA_ENVIDO)) System.out.println("  f) Subir con Falta Envido");
        System.out.print("Elegí una opción: ");
        String opcion = sc.nextLine().trim().toLowerCase();
        try {
            switch (opcion) {
                case "q" -> ronda.quieroEnvido();
                case "n" -> ronda.noQuieroEnvido();
                case "e", "r", "f" -> cantarTantos(ronda, quienResponde, opcion);
                default -> System.out.println("Opción inválida.");
            }
        } catch (JugadaInvalidaException e) {
            System.out.println("¡Jugada inválida! " + e.getMessage());
        }
    }

    /** Si se resolvió un envido o una flor, imprime el resultado (una sola vez). */
    private void anunciarTantos(Ronda ronda) {
        String anuncio = ronda.retirarAnuncioTantos();
        if (anuncio != null) {
            System.out.println();
            System.out.println(">>> " + anuncio.replace("\n", "\n    "));
        }
    }

    private void mostrarEstadoMesa(Ronda ronda) {
        List<Carta[]> jugadas = ronda.getCartasJugadasPorMano();
        System.out.println();
        System.out.println("Mano " + (ronda.getNumeroManoActual() + 1) + " de 3 | Se juega: " + ronda.getNivelTruco().getPuntos() + " punto(s)");
        for (int i = 0; i < jugadas.size(); i++) {
            Carta[] par = jugadas.get(i);
            System.out.println("  Mano " + (i + 1) + " -> " + par[0] + "  vs  " + par[1]);
        }
    }

    private void mostrarMano(Jugador jugador) {
        System.out.println(jugador.getNombre() + ", tus cartas:");
        List<Carta> mano = jugador.getMano();
        for (int i = 0; i < mano.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + mano.get(i));
        }
    }
}
