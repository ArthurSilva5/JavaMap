package app;

import ui.Menu;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        System.setProperty("http.agent", "JavaMapWeather/1.0 (academic project)");
        System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
        SwingUtilities.invokeLater(() -> {
            Menu tela = new Menu();
            tela.setLocationRelativeTo(null);
            tela.setVisible(true);
        });
    }
}
