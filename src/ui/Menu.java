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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
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

    private final EstacaoService service = new EstacaoService();
    private final MedicaoDAO medicaoDAO = new MedicaoDAO();
    private final OndaService ondaService = new OndaService();

    private final JXMapViewer mapa = MapaFactory.criar();
    private final EstacaoPainter estacaoPainter = new EstacaoPainter();
    private final AlagamentoPainter alagamentoPainter = new AlagamentoPainter();
    private final HeatmapPainter heatmapPainter = new HeatmapPainter();
    private final IsotermaPainter isotermaPainter = new IsotermaPainter();

    private List<Estacao> estacoesVisiveis = new ArrayList<>();
    private LocalDate inicioAtual = LocalDate.of(2026, 1, 1);
    private LocalDate fimAtual = LocalDate.of(2026, 5, 31);

    private final JTextField txtDataInicio = new JTextField("01/01/2026");
    private final JTextField txtDataFim = new JTextField("31/05/2026");
    private final JComboBox<String> cbCidade = new JComboBox<>();
    private final JComboBox<String> cbEstacao = new JComboBox<>();
    private final JComboBox<String> cbVariavel = new JComboBox<>(new String[]{"Temperatura", "Chuva", "Umidade"});
    private final JTextField txtLimiar = new JTextField("80");
    private final JComboBox<String> cbModo = new JComboBox<>(new String[]{"Marcadores", "Heatmap", "Zonas de Alerta", "Isócronas"});
    private final JComboBox<String> cbTendencia = new JComboBox<>(new String[]{"7 dias", "15 dias", "30 dias"});

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"Estação", "Cidade", "Temp", "Chuva", "Nível"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    private final JSlider sliderTempo = new JSlider(0, 100, 100);
    private final JLabel lblDataInicioSlider = new JLabel("01/01/2026");
    private final JLabel lblDataFimSlider = new JLabel("31/05/2026");
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

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, criarPainelEsquerdo(), criarPainelDireito());
        splitPane.setDividerLocation(320);
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
        painelEsquerdo.setPreferredSize(new Dimension(320, 768));
        painelEsquerdo.setBackground(Color.darkGray);

        JPanel painelFiltros = new JPanel(new GridLayout(9, 2, 5, 5));
        painelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelFiltros.setBackground(Color.darkGray);

        painelFiltros.add(rotulo("Data Início:"));
        painelFiltros.add(txtDataInicio);
        painelFiltros.add(rotulo("Data Fim:"));
        painelFiltros.add(txtDataFim);
        painelFiltros.add(rotulo("Cidade:"));
        painelFiltros.add(cbCidade);
        painelFiltros.add(rotulo("Estação:"));
        painelFiltros.add(cbEstacao);
        painelFiltros.add(rotulo("Variável:"));
        painelFiltros.add(cbVariavel);
        painelFiltros.add(rotulo("Limiar Alerta (mm):"));
        painelFiltros.add(txtLimiar);
        painelFiltros.add(rotulo("Visualização:"));
        painelFiltros.add(cbModo);
        painelFiltros.add(rotulo("Janela Tendência:"));
        painelFiltros.add(cbTendencia);

        JButton btnAplicar = new JButton("Filtrar");
        btnAplicar.setBackground(Color.lightGray);
        btnAplicar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAplicar.addActionListener(e -> atualizarMapa());
        painelFiltros.add(new JLabel(""));
        painelFiltros.add(btnAplicar);

        painelEsquerdo.add(painelFiltros, BorderLayout.NORTH);

        tabela.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        painelEsquerdo.add(scroll, BorderLayout.CENTER);

        return painelEsquerdo;
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

    private JLabel rotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        return label;
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
        atualizarTabela(filtradas);
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

    private void atualizarTabela(List<Estacao> estacoes) {
        modeloTabela.setRowCount(0);
        for (Estacao e : estacoes) {
            modeloTabela.addRow(new Object[]{
                    e.getId(),
                    e.getNome(),
                    numero(e.getTemperaturaMedia()),
                    numero(e.getPrecipitacaoTotal()),
                    e.getNivelAlerta().getRotulo()
            });
        }
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
