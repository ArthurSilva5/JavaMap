import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Menu tela = new Menu();
            
            tela.setLocationRelativeTo(null);

            tela.setVisible(true);

        });

    }
}