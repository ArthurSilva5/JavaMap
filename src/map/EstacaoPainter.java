package map;

import model.Estacao;
import model.Variavel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import util.Cores;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstacaoPainter implements Painter<JXMapViewer> {

    private static final int ZOOM_MAXIMO = 17;
    private static final int OSM_ZOOM_CLUSTER = 8;
    private static final int TAMANHO_CLUSTER = 55;

    private List<Estacao> estacoes = new ArrayList<>();
    private Variavel variavel = Variavel.TEMPERATURA;
    private final int diametro = 12;

    public void setEstacoes(List<Estacao> estacoes) {
        this.estacoes = estacoes;
    }

    public void setVariavel(Variavel variavel) {
        this.variavel = variavel;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer mapa, int largura, int altura) {
        g = (Graphics2D) g.create();
        Rectangle viewport = mapa.getViewportBounds();
        g.translate(-viewport.x, -viewport.y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int osmZoom = ZOOM_MAXIMO - mapa.getZoom();
        if (osmZoom < OSM_ZOOM_CLUSTER) {
            desenharClusters(g, mapa, viewport);
        } else {
            desenharMarcadores(g, mapa);
        }

        g.dispose();
    }

    private void desenharMarcadores(Graphics2D g, JXMapViewer mapa) {
        for (Estacao estacao : estacoes) {
            Point2D ponto = pixel(mapa, estacao);
            int x = (int) ponto.getX();
            int y = (int) ponto.getY();

            g.setColor(Cores.porVariavel(estacao.valorDe(variavel), variavel));
            g.fillOval(x - diametro / 2, y - diametro / 2, diametro, diametro);
            g.setColor(Color.BLACK);
            g.drawOval(x - diametro / 2, y - diametro / 2, diametro, diametro);
        }
    }

    private void desenharClusters(Graphics2D g, JXMapViewer mapa, Rectangle viewport) {
        Map<Long, List<Estacao>> grupos = new HashMap<>();
        Map<Long, Point2D> centros = new HashMap<>();

        for (Estacao estacao : estacoes) {
            Point2D ponto = pixel(mapa, estacao);
            long coluna = Math.round((ponto.getX() - viewport.x) / TAMANHO_CLUSTER);
            long linha = Math.round((ponto.getY() - viewport.y) / TAMANHO_CLUSTER);
            long chave = coluna * 100000L + linha;
            grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(estacao);
            centros.putIfAbsent(chave, ponto);
        }

        for (Map.Entry<Long, List<Estacao>> entrada : grupos.entrySet()) {
            List<Estacao> grupo = entrada.getValue();
            Point2D centro = centros.get(entrada.getKey());
            int x = (int) centro.getX();
            int y = (int) centro.getY();

            if (grupo.size() == 1) {
                g.setColor(Cores.porVariavel(grupo.get(0).valorDe(variavel), variavel));
                g.fillOval(x - diametro / 2, y - diametro / 2, diametro, diametro);
                g.setColor(Color.BLACK);
                g.drawOval(x - diametro / 2, y - diametro / 2, diametro, diametro);
            } else {
                int raio = 14 + Math.min(16, grupo.size());
                g.setColor(new Color(40, 90, 200, 200));
                g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                String texto = String.valueOf(grupo.size());
                int larguraTexto = g.getFontMetrics().stringWidth(texto);
                g.drawString(texto, x - larguraTexto / 2, y + 4);
            }
        }
    }

    private Point2D pixel(JXMapViewer mapa, Estacao estacao) {
        GeoPosition posicao = new GeoPosition(estacao.getLatitude(), estacao.getLongitude());
        return mapa.getTileFactory().geoToPixel(posicao, mapa.getZoom());
    }
}
