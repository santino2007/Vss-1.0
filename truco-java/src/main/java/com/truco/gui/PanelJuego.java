package com.truco.gui;

import com.truco.excepciones.JugadaInvalidaException;
import com.truco.logica.NivelTruco;
import com.truco.logica.Partida;
import com.truco.logica.Ronda;
import com.truco.logica.TipoEnvido;
import com.truco.modelo.Carta;
import com.truco.modelo.Jugador;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;

/**
 * Pantalla principal de juego (mesa de madera):
 *  - Izquierda: registro de eventos + botones de acción (Truco / Me voy / Quiero / No quiero).
 *  - Centro: cartas del rival (dadas vuelta), cartas jugadas en la mesa y tu mano.
 *  - Derecha: marcadores de puntaje, indicador de "MANO" y el mazo.
 * Siempre se dibuja desde la perspectiva del jugador que tiene que actuar
 * (el que está de turno o el que debe responder un canto).
 */
public class PanelJuego extends JPanel {

    private static final int MANO_W = 100, MANO_H = 150;
    private static final int RIVAL_W = 84, RIVAL_H = 126;
    private static final int MESA_W = 72, MESA_H = 108;

    private static final Color COLOR_ENVIDO = new Color(40, 110, 170);
    private static final Color COLOR_FLOR = new Color(140, 70, 160);
    private static final Color COLOR_QUIERO = new Color(40, 150, 70);
    private static final Color COLOR_NO_QUIERO = new Color(190, 50, 40);

    private final TrucoApp app;

    private final JTextArea logArea = new JTextArea();
    private final JLabel labelInfo = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel filaRival = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    private final JPanel mesa = new JPanel(new GridBagLayout());
    private final JPanel filaMano = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
    private final JPanel panelAcciones = new JPanel();

    private final JLabel manoRival = etiquetaMano(), nombreRival = etiquetaNombre(), puntosRival = etiquetaPuntos();
    private final JLabel manoYo = etiquetaMano(), nombreYo = etiquetaNombre(), puntosYo = etiquetaPuntos();

    private Ronda rondaLoggeada;  // para avisar en el log cuando empieza una ronda nueva
    private Jugador verComo;      // desde la perspectiva de quién se dibuja

    public PanelJuego(TrucoApp app) {
        this.app = app;
        setLayout(new BorderLayout(14, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(crearLateral(), BorderLayout.WEST);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearMarcadores(), BorderLayout.EAST);
    }

    // ------------------------------------------------------------------
    // Fondo de madera
    // ------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(112, 66, 38), 0, getHeight(), new Color(78, 44, 25)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Vetas de la madera (semilla fija para que no "parpadeen")
        Random rnd = new Random(42);
        g2.setStroke(new BasicStroke(1.6f));
        for (int i = 0; i < 45; i++) {
            int y = rnd.nextInt(Math.max(1, getHeight()));
            int x0 = rnd.nextInt(Math.max(1, getWidth()));
            int largo = 150 + rnd.nextInt(350);
            int onda = 6 + rnd.nextInt(10);
            g2.setColor(new Color(0, 0, 0, 14 + rnd.nextInt(20)));
            Path2D veta = new Path2D.Double();
            veta.moveTo(x0, y);
            veta.quadTo(x0 + largo / 2.0, y - onda, x0 + largo, y);
            g2.draw(veta);
        }
        g2.dispose();
    }

    // ------------------------------------------------------------------
    // Armado de la estructura (se hace una sola vez)
    // ------------------------------------------------------------------
    private JPanel crearLateral() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(200, 10));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(40, 25, 15));
        logArea.setForeground(new Color(240, 220, 190));
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logArea.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(25, 14, 8), 2));
        scroll.setPreferredSize(new Dimension(200, 190));
        scroll.setMaximumSize(new Dimension(200, 190));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelAcciones.setOpaque(false);
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(scroll);
        p.add(Box.createVerticalGlue());
        p.add(panelAcciones);
        return p;
    }

    private JPanel crearCentro() {
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.setOpaque(false);

        labelInfo.setForeground(Color.WHITE);
        labelInfo.setFont(new Font("SansSerif", Font.BOLD, 14));

        filaRival.setOpaque(false);
        filaRival.setPreferredSize(new Dimension(10, RIVAL_H + 16));
        filaMano.setOpaque(false);
        filaMano.setPreferredSize(new Dimension(10, MANO_H + 26));
        mesa.setOpaque(false);

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setOpaque(false);
        norte.add(labelInfo, BorderLayout.NORTH);
        norte.add(filaRival, BorderLayout.CENTER);

        c.add(norte, BorderLayout.NORTH);
        c.add(mesa, BorderLayout.CENTER);
        c.add(filaMano, BorderLayout.SOUTH);
        return c;
    }

    private JPanel crearMarcadores() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(170, 10));

        CartaVisual mazo = new CartaVisual(null, 90, 135);
        mazo.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(marcador(manoRival, nombreRival, puntosRival));
        p.add(Box.createVerticalGlue());
        p.add(mazo);
        p.add(Box.createVerticalGlue());
        p.add(marcador(manoYo, nombreYo, puntosYo));
        return p;
    }

    private JPanel marcador(JLabel mano, JLabel nombre, JLabel puntos) {
        JPanel m = new JPanel();
        m.setOpaque(false);
        m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
        m.add(mano);
        m.add(nombre);
        m.add(puntos);
        return m;
    }

    private static JLabel etiquetaMano() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private static JLabel etiquetaNombre() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(TrucoApp.COLOR_ACENTO);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(170, 32));
        return l;
    }

    private static JLabel etiquetaPuntos() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 64));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // ------------------------------------------------------------------
    // Actualización según el estado de la ronda
    // ------------------------------------------------------------------

    /** Reconstruye todo el contenido según el estado actual de la partida. */
    public void actualizar() {
        Partida partida = app.getPartida();
        Ronda ronda = partida.getRondaActual();

        // Perspectiva: quien debe actuar. Si la ronda ya terminó, se mantiene la anterior
        // para no mostrar por error las cartas del otro jugador.
        if (!ronda.isRondaTerminada() || verComo == null) {
            verComo = ronda.getJugadorQueDebeActuar();
        }
        Jugador yo = verComo;
        Jugador rival = partida.oponenteDe(yo);

        if (ronda != rondaLoggeada) {
            rondaLoggeada = ronda;
            log("— Nueva ronda: " + ronda.getJugadorMano().getNombre() + " es mano");
        }

        // Marcadores
        nombreRival.setText(rival.getNombre());
        puntosRival.setText(String.valueOf(rival.getPuntos()));
        manoRival.setText(ronda.getJugadorMano() == rival ? "MANO ▶" : " ");
        nombreYo.setText(yo.getNombre());
        puntosYo.setText(String.valueOf(yo.getPuntos()));
        manoYo.setText(ronda.getJugadorMano() == yo ? "MANO ▶" : " ");

        // Info de ronda
        String canto = ronda.getNivelTruco() == NivelTruco.SIN_CANTAR
                ? "sin truco (1 punto)"
                : ronda.getNivelTruco().nombreCanto() + " (" + ronda.getNivelTruco().getPuntos() + " puntos)";
        labelInfo.setText("Mano " + Math.min(ronda.getNumeroManoActual() + 1, 3) + " de 3   |   " + canto
                + "   |   a " + partida.getPuntosParaGanar() + " pts"
                + (ronda.isEnvidoPendiente()
                        ? "   |   Envido en juego: " + ronda.getPuntosEnvidoEnJuego() + " pts"
                        : ""));

        dibujarCartas(partida, ronda, yo, rival);
        armarAcciones(partida, ronda, yo);

        revalidate();
        repaint();
    }

    private void dibujarCartas(Partida partida, Ronda ronda, Jugador yo, Jugador rival) {
        // Rival: cartas dadas vuelta
        filaRival.removeAll();
        for (int i = 0; i < rival.getMano().size(); i++) {
            filaRival.add(new CartaVisual(null, RIVAL_W, RIVAL_H));
        }

        // Mi mano: solo se pueden jugar si es mi turno y no hay canto pendiente
        boolean puedoJugar = !ronda.isRondaTerminada() && !ronda.isRespuestaPendiente() && !ronda.isEnvidoPendiente()
                && ronda.getTurnoActual() == yo;
        filaMano.removeAll();
        List<Carta> mano = yo.getMano();
        for (int i = 0; i < mano.size(); i++) {
            int indice = i;
            CartaVisual cv = new CartaVisual(mano.get(i), MANO_W, MANO_H);
            if (puedoJugar) cv.setAccion(() -> jugarCarta(partida, ronda, yo, indice));
            filaMano.add(cv);
        }

        // Mesa: 3 bazas, cada una con carta del rival (arriba) y mía (abajo)
        mesa.removeAll();
        JPanel bazas = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        bazas.setOpaque(false);
        List<Carta[]> jugadas = ronda.getCartasJugadasPorMano();
        int idxYo = (yo == partida.getJugador1()) ? 0 : 1;
        for (int i = 0; i < 3; i++) {
            Carta cRival = null, cMia = null;
            if (i < jugadas.size()) {
                cMia = jugadas.get(i)[idxYo];
                cRival = jugadas.get(i)[1 - idxYo];
            } else if (i == jugadas.size() && !ronda.isRondaTerminada()) {
                cMia = ronda.getCartaEnMesa(yo);
                cRival = ronda.getCartaEnMesa(rival);
            }
            bazas.add(columnaBaza(i + 1, cRival, cMia));
        }
        mesa.add(bazas);
    }

    private JPanel columnaBaza(int numero, Carta cRival, Carta cMia) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Mano " + numero, SwingConstants.CENTER);
        titulo.setForeground(new Color(255, 255, 255, 190));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        col.add(titulo);
        col.add(Box.createVerticalStrut(4));
        col.add(celda(cRival));
        col.add(Box.createVerticalStrut(4));
        col.add(celda(cMia));
        return col;
    }

    private CartaVisual celda(Carta carta) {
        CartaVisual cv = carta != null ? new CartaVisual(carta, MESA_W, MESA_H) : CartaVisual.hueco(MESA_W, MESA_H);
        cv.setAlignmentX(Component.CENTER_ALIGNMENT);
        return cv;
    }

    // ------------------------------------------------------------------
    // Botones de acción
    // ------------------------------------------------------------------
    private void armarAcciones(Partida partida, Ronda ronda, Jugador yo) {
        panelAcciones.removeAll();
        if (!ronda.isRondaTerminada()) {
            NivelTruco siguiente = ronda.getNivelTruco().siguienteNivel();

            if (ronda.isEnvidoPendiente()) {
                armarRespuestaEnvido(partida, ronda, yo);
            } else if (ronda.isRespuestaPendiente()) {
                // "El envido está primero": antes de contestar el truco se puede cantar envido o flor.
                agregarBotonesTantos(partida, ronda, yo);
                agregarBoton(new BotonNaranja("QUIERO", COLOR_QUIERO), () -> {
                    ronda.quiero();
                    log(yo.getNombre() + ": ¡Quiero!");
                    app.irAPantallaDeTurno();
                });
                agregarBoton(new BotonNaranja("NO QUIERO", COLOR_NO_QUIERO), () -> {
                    ronda.noQuiero();
                    log(yo.getNombre() + ": No quiero.");
                    manejarFinDeRonda(partida, ronda);
                });
                if (siguiente != null) {
                    BotonNaranja subir = new BotonNaranja("QUIERO Y SUBO");
                    subir.setToolTipText("Quiero y canto " + siguiente.nombreCanto());
                    agregarBoton(subir, () -> {
                        ronda.quiero();
                        ronda.cantarTruco(yo);
                        log(yo.getNombre() + ": Quiero y " + ronda.getNivelTruco().nombreCanto());
                        app.irAPantallaDeTurno();
                    });
                }
            } else {
                agregarBotonesTantos(partida, ronda, yo);
                boolean puedeCantar = siguiente != null && ronda.getUltimoQueCanto() != yo;
                if (puedeCantar) {
                    agregarBoton(new BotonNaranja(siguiente.name().replace('_', ' ')), () -> {
                        try {
                            ronda.cantarTruco(yo);
                            log(yo.getNombre() + " cantó " + ronda.getNivelTruco().nombreCanto());
                            app.irAPantallaDeTurno();
                        } catch (JugadaInvalidaException ex) {
                            mostrarError(ex);
                        }
                    });
                }
                agregarBoton(new BotonNaranja("ME VOY", new Color(190, 50, 40)), () -> {
                    int r = JOptionPane.showConfirmDialog(this, "¿Seguro que querés irte al mazo?",
                            "Me voy", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) {
                        ronda.irseAlMazo(yo);
                        log(yo.getNombre() + " se fue al mazo.");
                        manejarFinDeRonda(partida, ronda);
                    }
                });
            }
        }
        panelAcciones.revalidate();
        panelAcciones.repaint();
    }

    /** Botones de Flor (si el jugador la tiene) y de Envido / Real Envido / Falta Envido (si corresponde). */
    private void agregarBotonesTantos(Partida partida, Ronda ronda, Jugador yo) {
        if (ronda.puedeCantarFlor(yo)) {
            agregarBoton(new BotonNaranja("FLOR", COLOR_FLOR), () ->
                    accionTantos(partida, ronda, yo.getNombre() + " cantó ¡Flor!", () -> ronda.cantarFlor(yo)));
        }
        if (ronda.puedeCantarEnvido(yo)) {
            for (TipoEnvido tipo : TipoEnvido.values()) {
                agregarBoton(new BotonNaranja(tipo.getEtiqueta(), COLOR_ENVIDO), () ->
                        accionTantos(partida, ronda, yo.getNombre() + " cantó " + tipo.getNombreCanto(),
                                () -> ronda.cantarEnvido(yo, tipo)));
            }
        }
    }

    /** Respuesta a un envido: Quiero, No quiero o subir la apuesta. */
    private void armarRespuestaEnvido(Partida partida, Ronda ronda, Jugador yo) {
        agregarBoton(new BotonNaranja("QUIERO", COLOR_QUIERO), () ->
                accionTantos(partida, ronda, yo.getNombre() + ": ¡Quiero!", ronda::quieroEnvido));
        agregarBoton(new BotonNaranja("NO QUIERO", COLOR_NO_QUIERO), () ->
                accionTantos(partida, ronda, yo.getNombre() + ": No quiero.", ronda::noQuieroEnvido));
        for (TipoEnvido tipo : ronda.tiposEnvidoParaSubir()) {
            agregarBoton(new BotonNaranja(tipo.getEtiqueta(), COLOR_ENVIDO), () ->
                    accionTantos(partida, ronda, yo.getNombre() + " subió: " + tipo.getNombreCanto(),
                            () -> ronda.cantarEnvido(yo, tipo)));
        }
    }

    /**
     * Ejecuta una acción de envido/flor, muestra el cartel con el resultado (si ya se resolvió)
     * y pasa el dispositivo al jugador que tiene que actuar ahora (o cierra la ronda).
     */
    private void accionTantos(Partida partida, Ronda ronda, String mensajeLog, Runnable accion) {
        try {
            accion.run();
        } catch (JugadaInvalidaException ex) {
            mostrarError(ex);
            return;
        }
        log(mensajeLog);
        String anuncio = ronda.retirarAnuncioTantos();
        if (anuncio != null) {
            log(anuncio.replace("\n", " — "));
            JOptionPane.showMessageDialog(this, anuncio, "Resultado de los tantos", JOptionPane.INFORMATION_MESSAGE);
        }
        if (ronda.isRondaTerminada()) {
            manejarFinDeRonda(partida, ronda);
        } else {
            app.irAPantallaDeTurno();
        }
    }

    private void agregarBoton(JButton boton, Runnable accion) {
        boton.addActionListener(e -> accion.run());
        panelAcciones.add(boton);
        panelAcciones.add(Box.createVerticalStrut(10));
    }

    // ------------------------------------------------------------------
    // Acciones del juego
    // ------------------------------------------------------------------
    private void jugarCarta(Partida partida, Ronda ronda, Jugador yo, int indice) {
        Carta carta = yo.getMano().get(indice);
        try {
            ronda.jugarCarta(yo, indice);
        } catch (JugadaInvalidaException ex) {
            mostrarError(ex);
            return;
        }
        log(yo.getNombre() + " jugó " + carta);
        if (ronda.isRondaTerminada()) {
            manejarFinDeRonda(partida, ronda);
        } else {
            app.irAPantallaDeTurno();
        }
    }

    /** Se llama cuando la ronda termina: muestra la mesa final, informa el resultado y avanza. */
    private void manejarFinDeRonda(Partida partida, Ronda ronda) {
        actualizar(); // para que se vea la última baza antes del cartel
        String motivo = ronda.isAlguienSeFueAlMazo()
                ? ronda.getJugadorSeFueAlMazo().getNombre() + " se fue al mazo."
                : ronda.isRondaCortadaPorPuntos()
                        ? ronda.getGanadorRonda().getNombre() + " llegó a los " + partida.getPuntosParaGanar()
                                + " puntos con los tantos."
                        : "Se definió jugando las cartas.";
        log("Ganó la ronda: " + ronda.getGanadorRonda().getNombre());
        JOptionPane.showMessageDialog(this,
                "Ganador de la ronda: " + ronda.getGanadorRonda().getNombre() + "\n" + motivo,
                "Fin de la ronda", JOptionPane.INFORMATION_MESSAGE);

        partida.cerrarRondaYContinuar();
        if (partida.isPartidaTerminada()) {
            app.mostrarFinPartida();
        } else {
            app.irAPantallaDeTurno();
        }
    }

    private void log(String mensaje) {
        logArea.append(mensaje + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void mostrarError(JugadaInvalidaException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Jugada inválida", JOptionPane.WARNING_MESSAGE);
    }
}