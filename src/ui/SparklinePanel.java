package ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

public class SparklinePanel extends JPanel {

    private final List<double[]> serie;

    public SparklinePanel(List<double[]> serie) {
        this.serie = serie;
        setPreferredSize(new Dimension(240, 80));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (serie == null || serie.size() < 2) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margem = 10;
        int largura = getWidth() - 2 * margem;
        int altura = getHeight() - 2 * margem;

        double minimo = Double.MAX_VALUE;
        double maximo = -Double.MAX_VALUE;
        for (double[] dia : serie) {
            minimo = Math.min(minimo, dia[1]);
            maximo = Math.max(maximo, dia[1]);
        }
        if (maximo - minimo < 1e-6) {
            maximo = minimo + 1;
        }

        int n = serie.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            double fracaoX = (double) i / (n - 1);
            double fracaoY = (serie.get(i)[1] - minimo) / (maximo - minimo);
            xs[i] = margem + (int) (fracaoX * largura);
            ys[i] = margem + (int) ((1 - fracaoY) * altura);
        }

        g.setColor(new Color(30, 100, 220));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i + 1 < n; i++) {
            g.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
        g.setColor(new Color(200, 50, 50));
        for (int i = 0; i < n; i++) {
            g.fillOval(xs[i] - 2, ys[i] - 2, 4, 4);
        }
    }
}
