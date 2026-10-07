package com.truco.gui;

import com.truco.logica.Partida;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación. Utiliza un CardLayout para navegar
 * entre las distintas pantallas del juego:
 *   1) Pantalla de inicio (nombres de los jugadores y puntaje objetivo).
 *   2) Pantalla de "pase de turno" (para que un jugador no vea las cartas del otro).
 *   3) Pantalla de juego (mesa, mano del jugador de turno, acciones de Truco).
 *   4) Pantalla de fin de partida (anuncia al ganador).
 */
public class TrucoApp extends JFrame {

    public static final Color COLOR_FONDO = new Color(84, 50, 29);    // madera
    public static final Color COLOR_PANEL = new Color(50, 30, 18);    // madera oscura
    public static final Color COLOR_ACENTO = new Color(255, 138, 20); // naranja
    public static final Color COLOR_TEXTO = Color.WHITE;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);

    // Contenedores "slot" fijos: se agregan UNA sola vez al CardLayout y su
    // contenido interno se reemplaza cada vez que hace falta mostrar una pantalla nueva.
    private final JPanel slotInicio = new JPanel(new BorderLayout());
    private final JPanel slotJuego = new JPanel(new BorderLayout());
    private final JPanel slotTurno = new JPanel(new BorderLayout());
    private final JPanel slotFin = new JPanel(new BorderLayout());

    private Partida partida;

    private PanelJuego panelJuego;

    public TrucoApp() {
        super("Truco Argentino - TP Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1120, 740);
        setLocationRelativeTo(null);

        contenedor.setBackground(COLOR_FONDO);
        add(contenedor);

        slotInicio.setBackground(COLOR_FONDO);
        slotJuego.setBackground(COLOR_FONDO);
        slotTurno.setBackground(COLOR_FONDO);
        slotFin.setBackground(COLOR_FONDO);
        contenedor.add(slotInicio, "inicio");
        contenedor.add(slotJuego, "juego");
        contenedor.add(slotTurno, "turno");
        contenedor.add(slotFin, "fin");

        mostrarInicio();
    }

    public void mostrarInicio() {
        slotInicio.removeAll();
        slotInicio.add(new PanelInicio(this), BorderLayout.CENTER);
        slotInicio.revalidate();
        slotInicio.repaint();
        cardLayout.show(contenedor, "inicio");
    }

    /** Se llama desde la pantalla de inicio cuando se confirman los datos y arranca la partida. */
    public void iniciarPartida(String nombre1, String nombre2, int puntos) {
        this.partida = new Partida(nombre1, nombre2, puntos);
        panelJuego = new PanelJuego(this);
        slotJuego.removeAll();
        slotJuego.add(panelJuego, BorderLayout.CENTER);
        slotJuego.revalidate();
        irAPantallaDeTurno();
    }

    public Partida getPartida() {
        return partida;
    }

    /**
     * Pantalla intermedia de "pasá el dispositivo" antes de mostrar la mano
     * del jugador que le toca actuar, para que el otro no vea sus cartas.
     */
    public void irAPantallaDeTurno() {
        // Quien debe actuar: el que responde un envido, el que responde un truco o el jugador de turno.
        String nombreSiguiente = partida.getRondaActual().getJugadorQueDebeActuar().getNombre();
        slotTurno.removeAll();
        slotTurno.add(new PanelTurno(this, nombreSiguiente, this::mostrarJuego), BorderLayout.CENTER);
        slotTurno.revalidate();
        slotTurno.repaint();
        cardLayout.show(contenedor, "turno");
    }

    public void mostrarJuego() {
        panelJuego.actualizar();
        cardLayout.show(contenedor, "juego");
    }

    public void mostrarFinPartida() {
        slotFin.removeAll();
        slotFin.add(new PanelFinPartida(this, partida.getGanadorPartida().getNombre()), BorderLayout.CENTER);
        slotFin.revalidate();
        slotFin.repaint();
        cardLayout.show(contenedor, "fin");
    }

    public void reiniciarAplicacion() {
        this.partida = null;
        mostrarInicio();
    }
}