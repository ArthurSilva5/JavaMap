package model;

public enum Variavel {

    TEMPERATURA("Temperatura", 0.0, 40.0, "C"),
    CHUVA("Chuva", 0.0, 200.0, "mm"),
    UMIDADE("Umidade", 0.0, 100.0, "%");

    private final String rotulo;
    private final double minEscala;
    private final double maxEscala;
    private final String unidade;

    Variavel(String rotulo, double minEscala, double maxEscala, String unidade) {
        this.rotulo = rotulo;
        this.minEscala = minEscala;
        this.maxEscala = maxEscala;
        this.unidade = unidade;
    }

    public String getRotulo() { return rotulo; }
    public double getMinEscala() { return minEscala; }
    public double getMaxEscala() { return maxEscala; }
    public String getUnidade() { return unidade; }

    public static Variavel porRotulo(String rotulo) {
        for (Variavel v : values()) {
            if (v.rotulo.equalsIgnoreCase(rotulo)) {
                return v;
            }
        }
        return TEMPERATURA;
    }
}
