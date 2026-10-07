package com.truco.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla inicial donde se ingresan los nombres de los dos jugadores
 * y se elige a cuántos puntos se juega la partida (15 o 30).
 */
public class PanelInicio extends JPanel {

    private final TrucoApp app;

    public PanelInicio(TrucoApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(TrucoApp.COLOR_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(TrucoApp.COLOR_PANEL);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel titulo = new JLabel("TRUCO ARGENTINO");
        titulo.setFont(new Font("Serif", Font.BOLD, 32));
        titulo.setForeground(TrucoApp.COLOR_ACENTO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Partida para 2 jugadores");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitulo.setForeground(TrucoApp.COLOR_TEXTO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField campoNombre1 = crearCampoTexto();
        JTextField campoNombre2 = crearCampoTexto();

        JLabel labelPuntos = new JLabel("Jugar a:");
        labelPuntos.setForeground(TrucoApp.COLOR_TEXTO);
        labelPuntos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JRadioButton radio15 = new JRadioButton("15 puntos", true);
        JRadioButton radio30 = new JRadioButton("30 puntos");
        for (JRadioButton r : new JRadioButton[]{radio15, radio30}) {
            r.setBackground(TrucoApp.COLOR_PANEL);
            r.setForeground(TrucoApp.COLOR_TEXTO);
            r.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radio15);
        grupo.add(radio30);

        JLabel error = new JLabel(" ");
        error.setForeground(Color.ORANGE);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton botonComenzar = new BotonNaranja("COMENZAR PARTIDA");
        botonComenzar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonComenzar.addActionListener(e -> {
            String n1 = campoNombre1.getText().trim();
            String n2 = campoNombre2.getText().trim();
            if (n1.isEmpty()) n1 = "Jugador 1";
            if (n2.isEmpty()) n2 = "Jugador 2";
            if (n1.equalsIgnoreCase(n2)) {
                error.setText("Los nombres deben ser distintos.");
                return;
            }
            int puntos = radio15.isSelected() ? 15 : 30;
            app.iniciarPartida(n1, n2, puntos);
        });

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(etiqueta("Nombre Jugador 1:"));
        tarjeta.add(campoNombre1);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(etiqueta("Nombre Jugador 2:"));
        tarjeta.add(campoNombre2);
        tarjeta.add(Box.createVerticalStrut(18));
        tarjeta.add(labelPuntos);
        JPanel filaRadios = new JPanel();
        filaRadios.setBackground(TrucoApp.COLOR_PANEL);
        filaRadios.add(radio15);
        filaRadios.add(radio30);
        filaRadios.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(filaRadios);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(error);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(botonComenzar);

        add(tarjeta);
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TrucoApp.COLOR_TEXTO);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField(16);
        campo.setMaximumSize(new Dimension(250, 32));
        campo.setAlignmentX(Component.CENTER_ALIGNMENT);
        return campo;
    }

    private void estilarBoton(JButton boton) {
        boton.setBackground(TrucoApp.COLOR_ACENTO);
        boton.setForeground(new Color(40, 30, 0));
        boton.setFont(new Font("SansSerif", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
