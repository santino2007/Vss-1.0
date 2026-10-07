package com.truco;

import com.truco.consola.JuegoConsola;
import com.truco.gui.TrucoApp;

import javax.swing.*;

/**
 * Punto de entrada del programa.
 *
 * Por defecto se abre la interfaz gráfica (Swing), ya que es más cómoda
 * para mostrar la partida en pantalla en lugar de ver todo por la terminal.
 *
 * Si se ejecuta con el argumento "consola" se corre la versión 100% texto:
 *   java -jar target/truco-argentino.jar consola
 */
public class Main {

    public static void main(String[] args) {
        boolean modoConsola = args.length > 0 && args[0].equalsIgnoreCase("consola");

        if (modoConsola) {
            new JuegoConsola().iniciar();
        } else {
            SwingUtilities.invokeLater(() -> {
                TrucoApp app = new TrucoApp();
                app.setVisible(true);
            });
        }
    }
}
