package model;

import java.awt.Color;

public enum NivelAlerta {

    NORMAL(0, "Normal", new Color(120, 120, 120, 0), 0),
    ATENCAO(1, "Atenção", new Color(255, 215, 0, 90), 1),
    ALERTA(2, "Alerta", new Color(255, 140, 0, 110), 2),
    EMERGENCIA(3, "Emergência", new Color(220, 0, 0, 130), 3);

    private final int grau;
    private final String rotulo;
    private final Color cor;
    private final int raio;

    NivelAlerta(int grau, String rotulo, Color cor, int raio) {
        this.grau = grau;
        this.rotulo = rotulo;
        this.cor = cor;
        this.raio = raio;
    }

    public int getGrau() { return grau; }
    public String getRotulo() { return rotulo; }
    public Color getCor() { return cor; }
    public int getRaio() { return raio; }

    public boolean emAlerta() {
        return grau >= ATENCAO.grau;
    }
}
