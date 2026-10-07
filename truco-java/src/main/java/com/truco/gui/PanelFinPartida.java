package com.truco.gui;

import javax.swing.*;
import java.awt.*;

/** Pantalla final que informa quién ganó la partida y permite jugar de nuevo. */
public class PanelFinPartida extends JPanel {

    public PanelFinPartida(TrucoApp app, String nombreGanador) {
        setLayout(new GridBagLayout());
        setBackground(TrucoApp.COLOR_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(TrucoApp.COLOR_PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TrucoApp.COLOR_ACENTO, 2, true),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel trofeo = new JLabel("\uD83C\uDFC6");
        trofeo.setFont(new Font("SansSerif", Font.PLAIN, 56));
        trofeo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mensaje = new JLabel("¡" + nombreGanador + " ganó la partida!");
        mensaje.setFont(new Font("SansSerif", Font.BOLD, 26));
        mensaje.setForeground(TrucoApp.COLOR_ACENTO);
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jugarDeNuevo = new BotonNaranja("JUGAR DE NUEVO");
        jugarDeNuevo.addActionListener(e -> app.reiniciarAplicacion());

        tarjeta.add(trofeo);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(mensaje);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(jugarDeNuevo);

        add(tarjeta);
    }
}