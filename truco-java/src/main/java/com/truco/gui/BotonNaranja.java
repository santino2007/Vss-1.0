package com.truco.gui;

import javax.swing.*;
import java.awt.*;

/** Botón redondeado con sombra, al estilo de los botones naranjas del diseño de referencia. */
public class BotonNaranja extends JButton {

    private final Color base;

    public BotonNaranja(String texto) {
        this(texto, TrucoApp.COLOR_ACENTO);
    }

    public BotonNaranja(String texto, Color base) {
        super(texto);
        this.base = base;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 15));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(200, 46);
        setPreferredSize(d);
        setMaximumSize(d);
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        Color c = !isEnabled() ? Color.GRAY
                : getModel().isPressed() ? base.darker()
                : getModel().isRollover() ? base.brighter() : base;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(2, 4, w - 4, h - 5, 20, 20);
        g2.setColor(c);
        g2.fillRoundRect(0, 0, w - 3, h - 6, 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
}