package map;

import model.Estacao;
import model.NivelAlerta;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class AlagamentoPainter implements Painter<JXMapViewer> {

    private List<Estacao> estacoes = new ArrayList<>();

    public void setEstacoes(List<Estacao> estacoes) {
        this.estacoes = estacoes;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer mapa, int largura, int altura) {
        g = (Graphics2D) g.create();
        Rectangle viewport = mapa.getViewportBounds();
        g.translate(-viewport.x, -viewport.y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Estacao estacao : estacoes) {
            NivelAlerta nivel = estacao.getNivelAlerta();
            if (nivel == null || !nivel.emAlerta()) {
                continue;
            }
            GeoPosition posicao = new GeoPosition(estacao.getLatitude(), estacao.getLongitude());
            Point2D ponto = mapa.getTileFactory().geoToPixel(posicao, mapa.getZoom());
            int x = (int) ponto.getX();
            int y = (int) ponto.getY();
            int raio = 14 + nivel.getRaio() * 16;

            g.setColor(nivel.getCor());
            g.fillOval(x - raio, y - raio, raio * 2, raio * 2);

            Color borda = nivel.getCor();
            g.setColor(new Color(borda.getRed(), borda.getGreen(), borda.getBlue(), 220));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x - raio, y - raio, raio * 2, raio * 2);
        }

        g.dispose();
    }
}
