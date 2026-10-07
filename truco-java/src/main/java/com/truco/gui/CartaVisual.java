package com.truco.gui;

import com.truco.modelo.Carta;
import com.truco.modelo.Palo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Carta de la baraja española dibujada con Java2D (sin imágenes externas).
 * Tiene tres modos:
 *  - frente (carta != null): palos españoles (oro, copa, espada, basto),
 *    con la disposición clásica de 1 a 7 y figuras para Sota (10), Caballo (11) y Rey (12).
 *  - dorso (carta == null)
 *  - hueco (lugar vacío en la mesa, con borde punteado)
 * Si se le asigna una acción, se levanta al pasar el mouse y ejecuta la acción al hacer click.
 */
public class CartaVisual extends JComponent {

    private static final int MARGEN = 14; // espacio arriba para el "salto" al hacer hover

    private final Carta carta;
    private final int ancho;
    private final int alto;
    private final boolean hueco;

    private Runnable accion;
    private boolean hover;

    public CartaVisual(Carta carta, int ancho, int alto) {
        this(carta, ancho, alto, false);
    }

    private CartaVisual(Carta carta, int ancho, int alto, boolean hueco) {
        this.carta = carta;
        this.ancho = ancho;
        this.alto = alto;
        this.hueco = hueco;
        Dimension d = new Dimension(ancho, alto + MARGEN);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    /** Lugar vacío (todavía no se jugó carta ahí). */
    public static CartaVisual hueco(int ancho, int alto) {
        return new CartaVisual(null, ancho, alto, true);
    }

    /** Hace la carta "clickeable". */
    public void setAccion(Runnable accion) {
        this.accion = accion;
        if (accion == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                if (contains(e.getPoint()) && CartaVisual.this.accion != null) {
                    CartaVisual.this.accion.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int cw = ancho - 5;
        int ch = alto - 5;
        g2.translate(0, (accion != null && hover) ? 0 : MARGEN);

        if (hueco) {
            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6, 6}, 0));
            g2.drawRoundRect(1, 1, cw - 2, ch - 2, 16, 16);
        } else {
            g2.setColor(new Color(0, 0, 0, 80)); // sombra
            g2.fillRoundRect(4, 4, cw, ch, 16, 16);
            if (carta == null) dibujarDorso(g2, cw, ch);
            else dibujarFrente(g2, cw, ch);
        }
        g2.dispose();
    }

    // ------------------------------------------------------------------
    // Frente de la carta
    // ------------------------------------------------------------------
    private void dibujarFrente(Graphics2D g2, int cw, int ch) {
        g2.setColor(new Color(0xFFFBEF));
        g2.fillRoundRect(0, 0, cw, ch, 16, 16);
        g2.setColor(new Color(60, 40, 20));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, cw - 2, ch - 2, 16, 16);

        int n = carta.getNumero();
        if (n >= 10) dibujarFigura(g2, cw, ch);
        else dibujarPalos(g2, cw, ch, n);

        // Número en la esquina superior izquierda y (girado) en la inferior derecha
        Font fNum = new Font("SansSerif", Font.BOLD, Math.max(12, cw / 5));
        String num = String.valueOf(n);
        g2.setColor(colorPalo(carta.getPalo()));
        g2.setFont(fNum);
        g2.drawString(num, 8, cw / 5 + 5);
        Graphics2D giro = (Graphics2D) g2.create();
        giro.rotate(Math.PI, cw / 2.0, ch / 2.0);
        giro.setFont(fNum);
        giro.drawString(num, 8, cw / 5 + 5);
        giro.dispose();
    }

    /** Cartas del 1 al 7: se repite el palo con la disposición clásica. */
    private void dibujarPalos(Graphics2D g2, int cw, int ch, int n) {
        double[][] pos;
        double s; // tamaño de cada palo (fracción del ancho)
        switch (n) {
            case 1 -> { pos = new double[][]{{.5, .5}}; s = .62; }
            case 2 -> { pos = new double[][]{{.5, .25}, {.5, .75}}; s = .34; }
            case 3 -> { pos = new double[][]{{.5, .22}, {.5, .5}, {.5, .78}}; s = .30; }
            case 4 -> { pos = new double[][]{{.33, .27}, {.67, .27}, {.33, .73}, {.67, .73}}; s = .28; }
            case 5 -> { pos = new double[][]{{.33, .27}, {.67, .27}, {.5, .5}, {.33, .73}, {.67, .73}}; s = .27; }
            case 6 -> { pos = new double[][]{{.33, .22}, {.67, .22}, {.33, .5}, {.67, .5}, {.33, .78}, {.67, .78}}; s = .26; }
            default -> { pos = new double[][]{{.33, .22}, {.67, .22}, {.5, .36}, {.33, .5}, {.67, .5}, {.33, .78}, {.67, .78}}; s = .24; }
        }
        for (double[] p : pos) {
            emblema(g2, carta.getPalo(), cw * p[0], ch * p[1], cw * s);
        }
    }

    /** Sota (10), Caballo (11) y Rey (12): un personaje simple con el palo en el pecho. */
    private void dibujarFigura(Graphics2D g2, int cw, int ch) {
        Palo palo = carta.getPalo();
        Color c = colorPalo(palo);
        int n = carta.getNumero();

        double px = cw * 0.2, py = ch * 0.17, pw = cw * 0.6, ph = ch * 0.66;
        RoundRectangle2D.Double panel = new RoundRectangle2D.Double(px, py, pw, ph, 12, 12);
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35));
        g2.fill(panel);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(panel);

        double cx = cw / 2.0;
        double r = pw * 0.2;
        double headY = py + ph * 0.36;

        // Cuerpo
        Path2D cuerpo = new Path2D.Double();
        cuerpo.moveTo(cx - pw * 0.42, py + ph - 3);
        cuerpo.lineTo(cx - pw * 0.22, py + ph * 0.52);
        cuerpo.quadTo(cx, py + ph * 0.44, cx + pw * 0.22, py + ph * 0.52);
        cuerpo.lineTo(cx + pw * 0.42, py + ph - 3);
        cuerpo.closePath();
        g2.setColor(c);
        g2.fill(cuerpo);
        g2.setColor(c.darker().darker());
        g2.draw(cuerpo);

        // Cabeza
        g2.setColor(new Color(242, 205, 165));
        g2.fill(new Ellipse2D.Double(cx - r, headY - r, r * 2, r * 2));
        g2.setColor(new Color(90, 60, 30));
        g2.draw(new Ellipse2D.Double(cx - r, headY - r, r * 2, r * 2));
        g2.fill(new Ellipse2D.Double(cx - r * 0.5 - 1.2, headY - r * 0.15, 2.4, 2.4));
        g2.fill(new Ellipse2D.Double(cx + r * 0.5 - 1.2, headY - r * 0.15, 2.4, 2.4));
        g2.draw(new Arc2D.Double(cx - r * 0.4, headY + r * 0.2, r * 0.8, r * 0.5, 200, 140, Arc2D.OPEN));
        if (n == 12) { // el rey lleva bigote
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawLine((int) (cx - r * 0.55), (int) (headY + r * 0.15), (int) (cx + r * 0.55), (int) (headY + r * 0.15));
            g2.setStroke(new BasicStroke(1.5f));
        }

        // Sombrero según la figura
        Color oscuro = c.darker();
        if (n == 10) { // Sota: gorro simple
            g2.setColor(oscuro);
            g2.fill(new Arc2D.Double(cx - r * 1.05, headY - r * 1.35, r * 2.1, r * 1.7, 0, 180, Arc2D.CHORD));
        } else if (n == 11) { // Caballo: sombrero con ala y pluma
            g2.setColor(oscuro);
            g2.fill(new Arc2D.Double(cx - r * 0.9, headY - r * 1.5, r * 1.8, r * 1.5, 0, 180, Arc2D.CHORD));
            g2.fill(new Ellipse2D.Double(cx - r * 1.5, headY - r * 0.95, r * 3, r * 0.5));
            g2.setColor(new Color(230, 190, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(cx + r * 0.2, headY - r * 2.1, r * 1.3, r * 1.4, 220, 120, Arc2D.OPEN));
            g2.setStroke(new BasicStroke(1.5f));
        } else { // Rey: corona
            double base = headY - r * 0.75;
            Path2D corona = new Path2D.Double();
            corona.moveTo(cx - r, base);
            corona.lineTo(cx - r, base - r * 0.9);
            corona.lineTo(cx - r * 0.5, base - r * 0.45);
            corona.lineTo(cx, base - r * 1.1);
            corona.lineTo(cx + r * 0.5, base - r * 0.45);
            corona.lineTo(cx + r, base - r * 0.9);
            corona.lineTo(cx + r, base);
            corona.closePath();
            g2.setColor(new Color(235, 185, 30));
            g2.fill(corona);
            g2.setColor(new Color(130, 90, 0));
            g2.draw(corona);
        }

        // Palo sobre el pecho
        emblema(g2, palo, cx, py + ph * 0.78, cw * 0.22);
    }

    // ------------------------------------------------------------------
    // Palos españoles (se dibujan en un cuadrado unitario de -0.5 a 0.5)
    // ------------------------------------------------------------------
    private void emblema(Graphics2D g2, Palo palo, double cx, double cy, double s) {
        Graphics2D g = (Graphics2D) g2.create();
        g.translate(cx, cy);
        g.scale(s, s);
        switch (palo) {
            case ORO -> dibujarOro(g);
            case COPA -> dibujarCopa(g);
            case ESPADA -> dibujarEspada(g);
            case BASTO -> dibujarBasto(g);
        }
        g.dispose();
    }

    private void dibujarOro(Graphics2D g) {
        g.setColor(new Color(232, 172, 24));
        g.fill(new Ellipse2D.Double(-0.5, -0.5, 1, 1));
        g.setColor(new Color(140, 90, 0));
        g.setStroke(new BasicStroke(0.06f));
        g.draw(new Ellipse2D.Double(-0.47, -0.47, 0.94, 0.94));
        g.setStroke(new BasicStroke(0.035f));
        g.draw(new Ellipse2D.Double(-0.28, -0.28, 0.56, 0.56));
        g.fill(new Ellipse2D.Double(-0.08, -0.08, 0.16, 0.16));
        // brillo
        g.setColor(new Color(255, 235, 150, 170));
        g.fill(new Arc2D.Double(-0.38, -0.38, 0.5, 0.5, 100, 90, Arc2D.OPEN));
    }

    private void dibujarCopa(Graphics2D g) {
        Color rojo = new Color(186, 32, 44);
        Color dorado = new Color(205, 145, 30);
        // pie y base
        g.setColor(dorado);
        g.fill(new RoundRectangle2D.Double(-0.06, 0.1, 0.12, 0.28, 0.05, 0.05));
        g.fill(new RoundRectangle2D.Double(-0.28, 0.34, 0.56, 0.14, 0.1, 0.1));
        // cuenco
        Path2D cuenco = new Path2D.Double();
        cuenco.moveTo(-0.45, -0.4);
        cuenco.lineTo(0.45, -0.4);
        cuenco.curveTo(0.45, 0.05, 0.22, 0.15, 0, 0.15);
        cuenco.curveTo(-0.22, 0.15, -0.45, 0.05, -0.45, -0.4);
        cuenco.closePath();
        g.setColor(rojo);
        g.fill(cuenco);
        g.setColor(new Color(90, 10, 20));
        g.setStroke(new BasicStroke(0.04f));
        g.draw(cuenco);
        // borde (vino)
        g.setColor(new Color(120, 15, 28));
        g.fill(new Ellipse2D.Double(-0.45, -0.47, 0.9, 0.14));
        // brillo
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new Ellipse2D.Double(-0.32, -0.22, 0.1, 0.22));
    }

    private void dibujarEspada(Graphics2D g) {
        // hoja
        Path2D hoja = new Path2D.Double();
        hoja.moveTo(0, -0.5);
        hoja.lineTo(0.1, -0.36);
        hoja.lineTo(0.08, 0.15);
        hoja.lineTo(-0.08, 0.15);
        hoja.lineTo(-0.1, -0.36);
        hoja.closePath();
        g.setColor(new Color(165, 185, 215));
        g.fill(hoja);
        g.setColor(new Color(30, 50, 110));
        g.setStroke(new BasicStroke(0.035f));
        g.draw(hoja);
        g.draw(new java.awt.geom.Line2D.Double(0, -0.4, 0, 0.12)); // canal central
        // guarda, empuñadura y pomo
        g.setColor(new Color(30, 60, 150));
        g.fill(new RoundRectangle2D.Double(-0.32, 0.15, 0.64, 0.08, 0.06, 0.06));
        g.setColor(new Color(120, 80, 30));
        g.fill(new RoundRectangle2D.Double(-0.04, 0.23, 0.08, 0.17, 0.03, 0.03));
        g.setColor(new Color(205, 145, 30));
        g.fill(new Ellipse2D.Double(-0.08, 0.38, 0.16, 0.12));
    }

    private void dibujarBasto(Graphics2D g) {
        Color madera = new Color(125, 82, 35);
        Color hoja = new Color(35, 135, 65);
        // hojas a los costados
        g.setColor(hoja);
        g.fill(new Ellipse2D.Double(-0.42, -0.32, 0.26, 0.14));
        g.fill(new Ellipse2D.Double(0.16, -0.12, 0.26, 0.14));
        g.fill(new Ellipse2D.Double(-0.40, 0.08, 0.24, 0.13));
        // palo
        Path2D palo = new Path2D.Double();
        palo.moveTo(-0.16, -0.5);
        palo.quadTo(0, -0.58, 0.16, -0.5);
        palo.lineTo(0.2, -0.2);
        palo.lineTo(0.07, 0.5);
        palo.lineTo(-0.07, 0.5);
        palo.lineTo(-0.2, -0.2);
        palo.closePath();
        g.setColor(madera);
        g.fill(palo);
        g.setColor(new Color(70, 40, 10));
        g.setStroke(new BasicStroke(0.04f));
        g.draw(palo);
        // nudos
        g.fill(new Ellipse2D.Double(-0.06, -0.25, 0.1, 0.07));
        g.fill(new Ellipse2D.Double(-0.03, 0.12, 0.08, 0.06));
    }

    // ------------------------------------------------------------------
    // Dorso
    // ------------------------------------------------------------------
    private void dibujarDorso(Graphics2D g2, int cw, int ch) {
        Color crema = new Color(0xF3EBC8);
        g2.setColor(crema);
        g2.fillRoundRect(0, 0, cw, ch, 16, 16);
        g2.setColor(new Color(0x1F6B63));
        g2.fillRoundRect(5, 5, cw - 10, ch - 10, 12, 12);
        g2.setColor(crema);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(12, 12, cw - 24, ch - 24, 8, 8);

        // Flor de 4 pétalos en el centro
        int cx = cw / 2, cy = ch / 2;
        for (int k = 0; k < 4; k++) {
            Graphics2D p = (Graphics2D) g2.create();
            p.rotate(k * Math.PI / 2, cx, cy);
            p.setColor(crema);
            p.fillOval(cx - cw / 14, cy - cw / 3, cw / 7, cw / 3);
            p.dispose();
        }
        g2.setColor(new Color(0x1F6B63));
        g2.fillOval(cx - cw / 16, cy - cw / 16, cw / 8, cw / 8);
    }

    private Color colorPalo(Palo palo) {
        return switch (palo) {
            case ESPADA -> new Color(25, 60, 150);
            case BASTO -> new Color(20, 110, 50);
            case ORO -> new Color(190, 130, 0);
            case COPA -> new Color(170, 20, 30);
        };
    }
}