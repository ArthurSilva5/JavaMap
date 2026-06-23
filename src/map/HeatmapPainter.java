package map;

import model.Estacao;
import model.Variavel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import util.Cores;
import util.Interpolacao;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class HeatmapPainter implements Painter<JXMapViewer> {

    private List<double[]> pontos = new ArrayList<>();
    private Variavel variavel = Variavel.TEMPERATURA;
    private final int celula = 18;

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
        if (pontos.isEmpty()) {
            return;
        }
        g = (Graphics2D) g.create();
        Rectangle viewport = mapa.getViewportBounds();
        int zoom = mapa.getZoom();
        double minimo = variavel.getMinEscala();
        double maximo = variavel.getMaxEscala();

        for (int x = 0; x < largura; x += celula) {
            for (int y = 0; y < altura; y += celula) {
                Point2D pixel = new Point2D.Double(viewport.x + x + celula / 2.0, viewport.y + y + celula / 2.0);
                GeoPosition geo = mapa.getTileFactory().pixelToGeo(pixel, zoom);
                double valor = Interpolacao.idw(geo.getLatitude(), geo.getLongitude(), pontos, 2.0);
                if (Double.isNaN(valor)) {
                    continue;
                }
                g.setColor(Cores.gradiente(valor, minimo, maximo, 110));
                g.fillRect(x, y, celula, celula);
            }
        }
        g.dispose();
    }
}
