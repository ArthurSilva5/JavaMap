package map;

import model.Estacao;
import model.Variavel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import util.Cores;
import util.Interpolacao;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IsotermaPainter implements Painter<JXMapViewer> {

    private List<double[]> pontos = new ArrayList<>();
    private Variavel variavel = Variavel.TEMPERATURA;
    private final int passo = 26;

    public void configurar(List<Estacao> estacoes, Variavel variavel) {
        this.variavel = variavel;
        this.pontos = new ArrayList<>();
        for (Estacao e : estacoes) {
            Double v = e.valorDe(variavel);
            if (v != null) {
                pontos.add(new double[]{e.getLatitude(), e.getLongitude(), v});
            }
        }
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer mapa, int largura, int altura) {
        if (pontos.size() < 3) {
            return;
        }
        g = (Graphics2D) g.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2f));

        Rectangle viewport = mapa.getViewportBounds();
        int zoom = mapa.getZoom();
        int colunas = largura / passo + 2;
        int linhas = altura / passo + 2;

        double[][] grade = new double[colunas][linhas];
        for (int i = 0; i < colunas; i++) {
            for (int j = 0; j < linhas; j++) {
                Point2D pixel = new Point2D.Double(viewport.x + i * passo, viewport.y + j * passo);
                GeoPosition geo = mapa.getTileFactory().pixelToGeo(pixel, zoom);
                grade[i][j] = Interpolacao.idw(geo.getLatitude(), geo.getLongitude(), pontos, 2.0);
            }
        }

        double minimo = variavel.getMinEscala();
        double maximo = variavel.getMaxEscala();
        double passoNivel = (maximo - minimo) / 12.0;

        for (double nivel = minimo + passoNivel; nivel < maximo; nivel += passoNivel) {
            g.setColor(Cores.gradiente(nivel, minimo, maximo, 255));
            for (int i = 0; i < colunas - 1; i++) {
                for (int j = 0; j < linhas - 1; j++) {
                    desenharCelula(g, nivel, i * passo, j * passo,
                            grade[i][j], grade[i + 1][j], grade[i + 1][j + 1], grade[i][j + 1]);
                }
            }
        }
        g.dispose();
    }

    private void desenharCelula(Graphics2D g, double c, int px, int py,
                                double vTL, double vTR, double vBR, double vBL) {
        List<Point2D> cruzamentos = new ArrayList<>();
        if (cruza(vTL, vTR, c)) cruzamentos.add(ponto(0, c, px, py, vTL, vTR, vBR, vBL));
        if (cruza(vTR, vBR, c)) cruzamentos.add(ponto(1, c, px, py, vTL, vTR, vBR, vBL));
        if (cruza(vBR, vBL, c)) cruzamentos.add(ponto(2, c, px, py, vTL, vTR, vBR, vBL));
        if (cruza(vBL, vTL, c)) cruzamentos.add(ponto(3, c, px, py, vTL, vTR, vBR, vBL));

        for (int k = 0; k + 1 < cruzamentos.size(); k += 2) {
            Point2D a = cruzamentos.get(k);
            Point2D b = cruzamentos.get(k + 1);
            g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
        }
    }

    private boolean cruza(double a, double b, double c) {
        return (a >= c) != (b >= c);
    }

    private Point2D ponto(int aresta, double c, int px, int py,
                          double vTL, double vTR, double vBR, double vBL) {
        switch (aresta) {
            case 0: return new Point2D.Double(px + fracao(c, vTL, vTR) * passo, py);
            case 1: return new Point2D.Double(px + passo, py + fracao(c, vTR, vBR) * passo);
            case 2: return new Point2D.Double(px + passo - fracao(c, vBR, vBL) * passo, py + passo);
            default: return new Point2D.Double(px, py + passo - fracao(c, vBL, vTL) * passo);
        }
    }

    private double fracao(double c, double a, double b) {
        double d = b - a;
        if (Math.abs(d) < 1e-9) {
            return 0.5;
        }
        double t = (c - a) / d;
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        return t;
    }
}
