import java.awt.*;
import javax.swing.*;

public class Menu extends JFrame {

    public Menu() {
        super("Mapa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        // Painel de Filtros
        JPanel painelEsquerdo = new JPanel(new BorderLayout());
        painelEsquerdo.setPreferredSize(new Dimension(300, 768));
        painelEsquerdo.setBackground(Color.darkGray);

        JPanel painelFiltros = new JPanel(new GridLayout(9, 2, 5, 5));
        painelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelFiltros.setBackground(Color.darkGray);

        // Labels e Campos de Texto
        JLabel lblDataInicio = new JLabel("Data Início:");
        lblDataInicio.setForeground(Color.WHITE);
        JTextField txtDataInicio = new JTextField(10);

        JLabel lblDataFim = new JLabel("Data Fim:");
        lblDataFim.setForeground(Color.WHITE);
        JTextField txtDataFim = new JTextField(10);

        JLabel lblCidade = new JLabel("Cidade:");
        lblCidade.setForeground(Color.WHITE);
        String[] cidades = {"Todas", "Porto Alegre", "Santa Maria", "Caxias do Sul"};
        JComboBox<String> cbCidade = new JComboBox<>(cidades);

        JLabel lblEstacao = new JLabel("Estação:");
        lblEstacao.setForeground(Color.WHITE);
        String[] estacoes = {"Todas", "A801", "A802", "A803", "OMM-83936"};
        JComboBox<String> cbEstacao = new JComboBox<>(estacoes);

        JLabel lblVariavel = new JLabel("Variável:");
        lblVariavel.setForeground(Color.WHITE);
        String[] variaveis = {"Temperatura", "Chuva", "Umidade"};
        JComboBox<String> cbVariavel = new JComboBox<>(variaveis);

        JLabel lblLimiar = new JLabel("Limiar Alerta (mm):");
        lblLimiar.setForeground(Color.WHITE);
        JTextField txtLimiar = new JTextField("80");

        JLabel lblModo = new JLabel("Visualização:");
        lblModo.setForeground(Color.WHITE);
        String[] modos = {"Marcadores", "Heatmap", "Zonas de Alerta", "Isócronas"};
        JComboBox<String> cbModo = new JComboBox<>(modos);

        JLabel lblTendencia = new JLabel("Janela Tendência:");
        lblTendencia.setForeground(Color.WHITE);
        String[] tendencias = {"7 dias", "15 dias", "30 dias"};
        JComboBox<String> cbTendencia = new JComboBox<>(tendencias);

        JButton btnAplicar = new JButton("Filtrar");
        btnAplicar.setBackground(Color.lightGray);
        btnAplicar.setFont(new Font("Arial", Font.BOLD, 14));

        // Adiciona os dados ao container 
        painelFiltros.add(lblDataInicio);
        painelFiltros.add(txtDataInicio);
        painelFiltros.add(lblDataFim);
        painelFiltros.add(txtDataFim);
        painelFiltros.add(lblCidade); 
        painelFiltros.add(cbCidade);  
        painelFiltros.add(lblEstacao);
        painelFiltros.add(cbEstacao);
        painelFiltros.add(lblVariavel);
        painelFiltros.add(cbVariavel);
        painelFiltros.add(lblLimiar);
        painelFiltros.add(txtLimiar);
        painelFiltros.add(lblModo);
        painelFiltros.add(cbModo);
        painelFiltros.add(lblTendencia);
        painelFiltros.add(cbTendencia);
        painelFiltros.add(new JLabel("")); 
        painelFiltros.add(btnAplicar);

        // Adiciona o container base ao container principal
        painelEsquerdo.add(painelFiltros, BorderLayout.NORTH);

        // Cria o painel Direito
        JPanel painelDireito = new JPanel(new BorderLayout());

        // Espaço para Renderezição do mapa
        JPanel mapPanel = new JPanel();
        mapPanel.setBackground(new Color(230, 230, 230));
        mapPanel.setLayout(new GridBagLayout());
        mapPanel.add(new JLabel("MAPA AQUI"));
        painelDireito.add(mapPanel, BorderLayout.CENTER);

        // Painel direito inferior
        JPanel painelSlider = new JPanel(new BorderLayout());
        painelSlider.setBackground(Color.darkGray);
        painelSlider.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblDataSliderMin = new JLabel("01/01/2026");
        lblDataSliderMin.setForeground(new Color(200, 50, 50));
        lblDataSliderMin.setFont(new Font("Arial", Font.BOLD, 14));

        JSlider sliderTempo = new JSlider(0, 100, 50);
        sliderTempo.setBackground(Color.darkGray);
        sliderTempo.setMajorTickSpacing(10);
        sliderTempo.setPaintTicks(true);

        JLabel lblDataSliderMax = new JLabel("01/05/2026");
        lblDataSliderMax.setForeground(new Color(200, 50, 50));
        lblDataSliderMax.setFont(new Font("Arial", Font.BOLD, 14));

        painelSlider.add(lblDataSliderMin, BorderLayout.WEST);
        painelSlider.add(sliderTempo, BorderLayout.CENTER);
        painelSlider.add(lblDataSliderMax, BorderLayout.EAST);

        painelDireito.add(painelSlider, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, painelDireito);
        splitPane.setDividerLocation(300);
        splitPane.setContinuousLayout(true);

        c.add(splitPane, BorderLayout.CENTER);
    }
}
