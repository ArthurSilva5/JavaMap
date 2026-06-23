package ui;

import dao.MedicaoDAO;
import map.AlagamentoPainter;
import map.EstacaoPainter;
import map.HeatmapPainter;
import map.IsotermaPainter;
import map.MapaFactory;
import model.Estacao;
import model.Variavel;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import service.EstacaoService;
import service.OndaService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Menu extends JFrame {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color FUNDO_PAINEL = new Color(33, 37, 43);
    private static final Color FUNDO_CAMPO = new Color(48, 53, 61);
    private static final Color BORDA_CAMPO = new Color(70, 77, 87);
    private static final Color TEXTO_ROTULO = new Color(168, 176, 187);
    private static final Color ACENTO = new Color(56, 132, 222);
    private static final Color ACENTO_HOVER = new Color(74, 148, 236);
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);

    private final EstacaoService service = new EstacaoService();
    private final MedicaoDAO medicaoDAO = new MedicaoDAO();
    private final OndaService ondaService = new OndaService();

    private final JXMapViewer mapa = MapaFactory.criar();
    private final EstacaoPainter estacaoPainter = new EstacaoPainter();
    private final AlagamentoPainter alagamentoPainter = new AlagamentoPainter();
    private final HeatmapPainter heatmapPainter = new HeatmapPainter();
    private final IsotermaPainter isotermaPainter = new IsotermaPainter();

    private List<Estacao> estacoesVisiveis = new ArrayList<>();
    private LocalDate inicioAtual = LocalDate.of(2025, 1, 1);
    private LocalDate fimAtual = LocalDate.of(2025, 5, 31);

    private final JTextField txtDataInicio = new JTextField("01/01/2025");
    private final JTextField txtDataFim = new JTextField("31/05/2025");
    private final JComboBox<String> cbCidade = new JComboBox<>();
    private final JComboBox<String> cbEstacao = new JComboBox<>();
    private final JComboBox<String> cbVariavel = new JComboBox<>(new String[]{"Temperatura", "Chuva", "Umidade"});
    private final JTextField txtLimiar = new JTextField("80");
    private final JComboBox<String> cbModo = new JComboBox<>(new String[]{"Marcadores", "Heatmap", "Zonas de Alerta", "Isócronas"});
    private final JComboBox<String> cbTendencia = new JComboBox<>(new String[]{"7 dias", "15 dias", "30 dias"});

    private final JSlider sliderTempo = new JSlider(0, 100, 100);
    private final JLabel lblDataInicioSlider = new JLabel("01/01/2025");
    private final JLabel lblDataFimSlider = new JLabel("31/05/2025");
    private final JLabel lblDataAtual = new JLabel(" ");
    private final JButton btnPlay = new JButton("Play");
    private final Timer timerAnimacao = new Timer(450, e -> avancarSlider());
    private int diaAtualSlider = -1;
    private boolean ajustandoSlider = false;
    private LocalDate diaExibido = fimAtual;

    public Menu() {
        super("Mapa de Estações Meteorológicas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 768);

        configurarTemaCombos();

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, criarPainelEsquerdo(), criarPainelDireito());
        splitPane.setDividerLocation(340);
        splitPane.setContinuousLayout(true);
        c.add(splitPane, BorderLayout.CENTER);

        mapa.setOverlayPainter(estacaoPainter);
        adicionarControlesZoom();
        configurarCliqueNoMarcador();
        configurarListeners();
        carregarCombos();
        atualizarMapa();
    }

    private void adicionarControlesZoom() {
        JButton btnZoomMais = new JButton("+");
        JButton btnZoomMenos = new JButton("-");
        btnZoomMais.setFont(new Font("Arial", Font.BOLD, 18));
        btnZoomMenos.setFont(new Font("Arial", Font.BOLD, 18));
        btnZoomMais.setPreferredSize(new Dimension(46, 30));
        btnZoomMenos.setPreferredSize(new Dimension(46, 30));
        btnZoomMais.setFocusable(false);
        btnZoomMenos.setFocusable(false);
        btnZoomMais.setToolTipText("Aproximar");
        btnZoomMenos.setToolTipText("Afastar");
        btnZoomMais.addActionListener(e -> aplicarZoom(-1));
        btnZoomMenos.addActionListener(e -> aplicarZoom(1));

        JPanel botoes = new JPanel(new GridLayout(2, 1, 0, 4));
        botoes.setOpaque(false);
        botoes.add(btnZoomMais);
        botoes.add(btnZoomMenos);

        JPanel canto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        canto.setOpaque(false);
        canto.add(botoes);

        mapa.setLayout(new BorderLayout());
        mapa.add(canto, BorderLayout.NORTH);
    }

    private void aplicarZoom(int delta) {
        int novo = mapa.getZoom() + delta;
        int minimo = mapa.getTileFactory().getInfo().getMinimumZoomLevel();
        int maximo = mapa.getTileFactory().getInfo().getMaximumZoomLevel();
        if (novo < minimo) {
            novo = minimo;
        }
        if (novo > maximo) {
            novo = maximo;
        }
        mapa.setZoom(novo);
        mapa.repaint();
    }

    private void configurarListeners() {
        cbModo.addActionListener(e -> {
            aplicarModo();
            mapa.repaint();
        });
        cbVariavel.addActionListener(e -> {
            Variavel variavel = Variavel.porRotulo((String) cbVariavel.getSelectedItem());
            estacaoPainter.setVariavel(variavel);
            heatmapPainter.configurar(estacoesVisiveis, variavel);
            isotermaPainter.configurar(estacoesVisiveis, variavel);
            mapa.repaint();
        });
    }

    private JPanel criarPainelEsquerdo() {
        JPanel painelEsquerdo = new JPanel(new BorderLayout());
        painelEsquerdo.setPreferredSize(new Dimension(340, 768));
        painelEsquerdo.setBackground(FUNDO_PAINEL);

        painelEsquerdo.add(criarCabecalho(), BorderLayout.NORTH);

        JPanel painelFiltros = new JPanel();
        painelFiltros.setLayout(new BoxLayout(painelFiltros, BoxLayout.Y_AXIS));
        painelFiltros.setBackground(FUNDO_PAINEL);
        painelFiltros.setBorder(BorderFactory.createEmptyBorder(8, 22, 22, 22));

        painelFiltros.add(criarCampo("DATA INÍCIO", estilizarTexto(txtDataInicio)));
        painelFiltros.add(criarCampo("DATA FIM", estilizarTexto(txtDataFim)));
        painelFiltros.add(criarCampo("CIDADE", estilizarCombo(cbCidade)));
        painelFiltros.add(criarCampo("ESTAÇÃO", estilizarCombo(cbEstacao)));
        painelFiltros.add(criarCampo("VARIÁVEL", estilizarCombo(cbVariavel)));
        painelFiltros.add(criarCampo("LIMIAR DE ALERTA (mm)", estilizarTexto(txtLimiar)));
        painelFiltros.add(criarCampo("VISUALIZAÇÃO", estilizarCombo(cbModo)));
        painelFiltros.add(criarCampo("JANELA DE TENDÊNCIA", estilizarCombo(cbTendencia)));

        painelFiltros.add(Box.createVerticalGlue());
        painelFiltros.add(criarBotaoFiltrar());

        painelEsquerdo.add(painelFiltros, BorderLayout.CENTER);

        return painelEsquerdo;
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setBackground(FUNDO_PAINEL);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(22, 22, 14, 22));

        JLabel titulo = new JLabel("Estações Meteorológicas");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(FONTE_TITULO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Filtros de visualização");
        subtitulo.setForeground(TEXTO_ROTULO);
        subtitulo.setFont(FONTE_SUBTITULO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        cabecalho.add(titulo);
        cabecalho.add(subtitulo);
        return cabecalho;
    }

    private JPanel criarCampo(String rotuloTexto, JComponent campo) {
        JPanel bloco = new JPanel();
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setBackground(FUNDO_PAINEL);
        bloco.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloco.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bloco.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel label = new JLabel(rotuloTexto);
        label.setForeground(TEXTO_ROTULO);
        label.setFont(FONTE_ROTULO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        campo.setAlignmentX(Component.LEFT_ALIGNMENT);

        bloco.add(label);
        bloco.add(campo);
        return bloco;
    }

    private JTextField estilizarTexto(JTextField campo) {
        campo.setBackground(FUNDO_CAMPO);
        campo.setForeground(Color.WHITE);
        campo.setCaretColor(Color.WHITE);
        campo.setFont(FONTE_CAMPO);
        campo.setBorder(bordaCampo());
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setPreferredSize(new Dimension(0, 34));
        return campo;
    }

    private void configurarTemaCombos() {
        UIManager.put("ComboBox.background", new ColorUIResource(FUNDO_CAMPO));
        UIManager.put("ComboBox.foreground", new ColorUIResource(Color.WHITE));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(ACENTO));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.WHITE));
    }

    private JComboBox<String> estilizarCombo(JComboBox<String> combo) {
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return criarSetaCombo();
            }
        });
        combo.setBackground(FUNDO_CAMPO);
        combo.setForeground(Color.WHITE);
        combo.setFont(FONTE_CAMPO);
        combo.setBorder(BorderFactory.createLineBorder(BORDA_CAMPO));
        combo.setFocusable(false);
        combo.setRenderer(new ComboRenderer());
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        combo.setPreferredSize(new Dimension(0, 34));
        return combo;
    }

    private JButton criarSetaCombo() {
        JButton seta = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FUNDO_CAMPO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int[] xs = {cx - 4, cx + 4, cx};
                int[] ys = {cy - 2, cy - 2, cy + 3};
                g2.setColor(TEXTO_ROTULO);
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        seta.setBorder(BorderFactory.createEmptyBorder());
        seta.setContentAreaFilled(false);
        seta.setFocusable(false);
        seta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        seta.setPreferredSize(new Dimension(26, 34));
        return seta;
    }

    private static class ComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean selecionado, boolean comFoco) {
            super.getListCellRendererComponent(lista, valor, indice, selecionado, comFoco);
            setBackground(selecionado ? ACENTO : FUNDO_CAMPO);
            setForeground(Color.WHITE);
            setFont(FONTE_CAMPO);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return this;
        }
    }

    private Border bordaCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA_CAMPO),
                BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    private JButton criarBotaoFiltrar() {
        JButton btnAplicar = new JButton("Filtrar");
        btnAplicar.setFont(FONTE_BOTAO);
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setBackground(ACENTO);
        btnAplicar.setFocusPainted(false);
        btnAplicar.setBorderPainted(false);
        btnAplicar.setOpaque(true);
        btnAplicar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAplicar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAplicar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnAplicar.setPreferredSize(new Dimension(0, 42));
        btnAplicar.addActionListener(e -> atualizarMapa());
        btnAplicar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent ev) {
                btnAplicar.setBackground(ACENTO_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent ev) {
                btnAplicar.setBackground(ACENTO);
            }
        });
        return btnAplicar;
    }

    private JPanel criarPainelDireito() {
        JPanel painelDireito = new JPanel(new BorderLayout());
        painelDireito.add(mapa, BorderLayout.CENTER);

        JPanel painelSlider = new JPanel(new BorderLayout());
        painelSlider.setBackground(Color.darkGray);
        painelSlider.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        lblDataInicioSlider.setForeground(new Color(200, 50, 50));
        lblDataInicioSlider.setFont(new Font("Arial", Font.BOLD, 14));
        lblDataFimSlider.setForeground(new Color(200, 50, 50));
        lblDataFimSlider.setFont(new Font("Arial", Font.BOLD, 14));

        sliderTempo.setBackground(Color.darkGray);
        sliderTempo.setPaintTicks(true);
        sliderTempo.addChangeListener(e -> aoMoverSlider());

        btnPlay.addActionListener(e -> alternarAnimacao());
        lblDataAtual.setForeground(Color.WHITE);
        lblDataAtual.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controles.setBackground(Color.darkGray);
        controles.add(btnPlay);
        controles.add(lblDataAtual);

        JPanel scrubber = new JPanel(new BorderLayout());
        scrubber.setBackground(Color.darkGray);
        scrubber.add(lblDataInicioSlider, BorderLayout.WEST);
        scrubber.add(sliderTempo, BorderLayout.CENTER);
        scrubber.add(lblDataFimSlider, BorderLayout.EAST);

        painelSlider.add(controles, BorderLayout.NORTH);
        painelSlider.add(scrubber, BorderLayout.CENTER);

        painelDireito.add(painelSlider, BorderLayout.SOUTH);
        return painelDireito;
    }

    private void carregarCombos() {
        cbCidade.addItem("Todas");
        for (String cidade : service.cidades()) {
            cbCidade.addItem(cidade);
        }
        cbEstacao.addItem("Todas");
        for (String id : service.ids()) {
            cbEstacao.addItem(id);
        }
    }

    private void atualizarMapa() {
        LocalDate inicio;
        LocalDate fim;
        try {
            inicio = LocalDate.parse(txtDataInicio.getText().trim(), FMT);
            fim = LocalDate.parse(txtDataFim.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Use o formato dd/MM/aaaa nas datas.", "Filtro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fim.isBefore(inicio)) {
            JOptionPane.showMessageDialog(this, "A data fim deve ser maior ou igual à data início.", "Filtro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (timerAnimacao.isRunning()) {
            alternarAnimacao();
        }
        inicioAtual = inicio;
        fimAtual = fim;
        configurarSlider();
    }

    private void configurarSlider() {
        int totalDias = (int) ChronoUnit.DAYS.between(inicioAtual, fimAtual);
        ajustandoSlider = true;
        sliderTempo.setMinimum(0);
        sliderTempo.setMaximum(totalDias);
        sliderTempo.setMajorTickSpacing(Math.max(1, totalDias / 10));
        sliderTempo.setValue(totalDias);
        lblDataInicioSlider.setText(inicioAtual.format(FMT));
        lblDataFimSlider.setText(fimAtual.format(FMT));
        ajustandoSlider = false;
        diaAtualSlider = -1;
        aoMoverSlider();
    }

    private void aoMoverSlider() {
        if (ajustandoSlider) {
            return;
        }
        int valor = sliderTempo.getValue();
        if (valor == diaAtualSlider) {
            return;
        }
        diaAtualSlider = valor;
        renderizarDia(inicioAtual.plusDays(valor));
    }

    private void renderizarDia(LocalDate dia) {
        diaExibido = dia;
        Variavel variavel = Variavel.porRotulo((String) cbVariavel.getSelectedItem());
        List<Estacao> dados = service.carregar(dia, dia, janelaTendencia(), limiar());

        String cidade = (String) cbCidade.getSelectedItem();
        String estacao = (String) cbEstacao.getSelectedItem();
        List<Estacao> filtradas = new ArrayList<>();
        for (Estacao e : dados) {
            if (estacao != null && !"Todas".equals(estacao) && !e.getId().equals(estacao)) {
                continue;
            }
            if (cidade != null && !"Todas".equals(cidade) && !cidade.equals(e.getNome())) {
                continue;
            }
            filtradas.add(e);
        }

        estacoesVisiveis = filtradas;
        estacaoPainter.setEstacoes(filtradas);
        estacaoPainter.setVariavel(variavel);
        alagamentoPainter.setEstacoes(filtradas);
        heatmapPainter.configurar(filtradas, variavel);
        isotermaPainter.configurar(filtradas, variavel);

        aplicarModo();
        lblDataAtual.setText("Dia exibido: " + dia.format(FMT));
        mapa.repaint();
    }

    private void alternarAnimacao() {
        if (timerAnimacao.isRunning()) {
            timerAnimacao.stop();
            btnPlay.setText("Play");
        } else {
            timerAnimacao.start();
            btnPlay.setText("Pausar");
        }
    }

    private void avancarSlider() {
        int valor = sliderTempo.getValue();
        if (valor >= sliderTempo.getMaximum()) {
            valor = sliderTempo.getMinimum();
        } else {
            valor++;
        }
        sliderTempo.setValue(valor);
    }

    private void aplicarModo() {
        String modo = (String) cbModo.getSelectedItem();
        Painter<JXMapViewer> overlay;
        boolean tendencia = false;
        if ("Heatmap".equals(modo)) {
            overlay = new CompoundPainter<JXMapViewer>(heatmapPainter, estacaoPainter);
        } else if ("Zonas de Alerta".equals(modo)) {
            overlay = new CompoundPainter<JXMapViewer>(alagamentoPainter, estacaoPainter);
        } else if ("Isócronas".equals(modo)) {
            overlay = new CompoundPainter<JXMapViewer>(isotermaPainter, estacaoPainter);
        } else {
            overlay = estacaoPainter;
            tendencia = true;
        }
        estacaoPainter.setMostrarTendencia(tendencia);
        mapa.setOverlayPainter(overlay);
    }

    private int janelaTendencia() {
        String texto = (String) cbTendencia.getSelectedItem();
        try {
            return Integer.parseInt(texto.split(" ")[0]);
        } catch (RuntimeException ex) {
            return 7;
        }
    }

    private double limiar() {
        try {
            return Double.parseDouble(txtLimiar.getText().trim());
        } catch (RuntimeException ex) {
            return 0.0;
        }
    }

    private void configurarCliqueNoMarcador() {
        mapa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                if (!SwingUtilities.isLeftMouseButton(ev)) {
                    return;
                }
                Estacao alvo = estacaoMaisProxima(ev.getPoint());
                if (alvo != null) {
                    mostrarDetalhes(alvo);
                }
            }
        });
    }

    private Estacao estacaoMaisProxima(Point ponto) {
        Rectangle viewport = mapa.getViewportBounds();
        Estacao melhor = null;
        double menorDistancia = 12.0;
        for (Estacao e : estacoesVisiveis) {
            GeoPosition posicao = new GeoPosition(e.getLatitude(), e.getLongitude());
            Point2D pt = mapa.getTileFactory().geoToPixel(posicao, mapa.getZoom());
            double dx = pt.getX() - viewport.x - ponto.x;
            double dy = pt.getY() - viewport.y - ponto.y;
            double distancia = Math.hypot(dx, dy);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhor = e;
            }
        }
        return melhor;
    }

    private void mostrarDetalhes(Estacao e) {
        JPanel painel = new JPanel(new BorderLayout(8, 8));

        String html = "<html><b>" + e.getNome() + "</b> (" + e.getId() + ")<br>"
                + "QC: " + valor(e.getQcStatus()) + "<br><br>"
                + "Temperatura média: " + numero(e.getTemperaturaMedia()) + " C<br>"
                + "Mínima: " + numero(e.getTemperaturaMin()) + " C<br>"
                + "Máxima: " + numero(e.getTemperaturaMax()) + " C<br>"
                + "Umidade média: " + numero(e.getUmidadeMedia()) + " %<br>"
                + "Chuva acumulada: " + numero(e.getPrecipitacaoTotal()) + " mm<br><br>"
                + "mm24/48/72: " + arredonda(e.getMm24()) + " / " + arredonda(e.getMm48()) + " / " + arredonda(e.getMm72()) + "<br>"
                + "Nível de alerta: <b>" + e.getNivelAlerta().getRotulo() + "</b><br>"
                + "Tendência de chuva: " + (e.getTendenciaChuva() > 0.05 ? "crescente" : e.getTendenciaChuva() < -0.05 ? "decrescente" : "estável")
                + "</html>";
        painel.add(new JLabel(html), BorderLayout.NORTH);

        List<double[]> serie = medicaoDAO.serieTemperatura(e.getId(), diaExibido.minusDays(6), diaExibido);
        painel.add(new SparklinePanel(serie), BorderLayout.CENTER);

        String onda = ondaService.diagnosticar(e.getId(), inicioAtual, fimAtual);
        painel.add(new JLabel("<html><b>Ondas:</b> " + onda + "</html>"), BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, painel, "Detalhes - " + e.getNome(), JOptionPane.INFORMATION_MESSAGE);
    }

    private String numero(Double valor) {
        return valor == null ? "sem dados" : String.format("%.1f", valor);
    }

    private String arredonda(double valor) {
        return String.format("%.0f", valor);
    }

    private String valor(String texto) {
        return texto == null ? "-" : texto;
    }
}
