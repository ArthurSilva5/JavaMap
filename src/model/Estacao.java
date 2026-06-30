package model;

public class Estacao {

    private String id;
    private String nome;
    private double latitude;
    private double longitude;
    private String qcStatus;
    private Double temperaturaMedia;
    private Double temperaturaMin;
    private Double temperaturaMax;
    private Double precipitacaoTotal;
    private Double umidadeMedia;
    private double mm24;
    private double mm48;
    private double mm72;
    private NivelAlerta nivelAlerta = NivelAlerta.NORMAL;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getQcStatus() { return qcStatus; }
    public void setQcStatus(String qcStatus) { this.qcStatus = qcStatus; }

    public Double getTemperaturaMedia() { return temperaturaMedia; }
    public void setTemperaturaMedia(Double temperaturaMedia) { this.temperaturaMedia = temperaturaMedia; }

    public Double getTemperaturaMin() { return temperaturaMin; }
    public void setTemperaturaMin(Double temperaturaMin) { this.temperaturaMin = temperaturaMin; }

    public Double getTemperaturaMax() { return temperaturaMax; }
    public void setTemperaturaMax(Double temperaturaMax) { this.temperaturaMax = temperaturaMax; }

    public Double getPrecipitacaoTotal() { return precipitacaoTotal; }
    public void setPrecipitacaoTotal(Double precipitacaoTotal) { this.precipitacaoTotal = precipitacaoTotal; }

    public Double getUmidadeMedia() { return umidadeMedia; }
    public void setUmidadeMedia(Double umidadeMedia) { this.umidadeMedia = umidadeMedia; }

    public double getMm24() { return mm24; }
    public void setMm24(double mm24) { this.mm24 = mm24; }

    public double getMm48() { return mm48; }
    public void setMm48(double mm48) { this.mm48 = mm48; }

    public double getMm72() { return mm72; }
    public void setMm72(double mm72) { this.mm72 = mm72; }

    public NivelAlerta getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(NivelAlerta nivelAlerta) { this.nivelAlerta = nivelAlerta; }

    public Double valorDe(Variavel variavel) {
        switch (variavel) {
            case TEMPERATURA: return temperaturaMedia;
            case CHUVA: return precipitacaoTotal;
            case UMIDADE: return umidadeMedia;
            default: return null;
        }
    }
}
