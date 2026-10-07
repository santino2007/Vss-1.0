package com.truco.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla que se muestra entre turnos: oculta las cartas del jugador
 * anterior y le pide al siguiente jugador que confirme para ver su mano.
 * Es necesaria porque el juego es "hotseat" (los dos jugadores comparten
 * la misma pantalla/dispositivo).
 */
public class PanelTurno extends JPanel {

    public PanelTurno(TrucoApp app, String nombreSiguiente, Runnable alContinuar) {
        setLayout(new GridBagLayout());
        setBackground(TrucoApp.COLOR_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(TrucoApp.COLOR_PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TrucoApp.COLOR_ACENTO, 2, true),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel icono = new JLabel("\uD83C\uDCCF"); // naipe
        icono.setFont(new Font("SansSerif", Font.PLAIN, 48));
        icono.setForeground(TrucoApp.COLOR_TEXTO);
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mensaje = new JLabel("Turno de " + nombreSiguiente);
        mensaje.setFont(new Font("SansSerif", Font.BOLD, 26));
        mensaje.setForeground(TrucoApp.COLOR_ACENTO);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ayuda = new JLabel("Pasá el dispositivo y presioná Continuar");
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ayuda.setForeground(TrucoApp.COLOR_TEXTO);
        ayuda.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton continuar = new BotonNaranja("CONTINUAR");
        continuar.addActionListener(e -> alContinuar.run());

        tarjeta.add(icono);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(mensaje);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(ayuda);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(continuar);

        add(tarjeta);
    }
}