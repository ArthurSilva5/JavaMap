package util;

import model.Variavel;

import java.awt.Color;

public class Cores {

    public static Color porTemperatura(Double temperatura) {
        return porVariavel(temperatura, Variavel.TEMPERATURA);
    }

    public static Color porVariavel(Double valor, Variavel variavel) {
        if (valor == null) {
            return new Color(150, 150, 150);
        }
        return gradiente(valor, variavel.getMinEscala(), variavel.getMaxEscala(), 255);
    }

    public static Color gradiente(double valor, double minimo, double maximo, int alfa) {
        double fracao = (valor - minimo) / (maximo - minimo);
        return gradienteFracao(fracao, alfa);
    }

    public static Color gradienteFracao(double fracao, int alfa) {
        if (fracao < 0) fracao = 0;
        if (fracao > 1) fracao = 1;
        int vermelho;
        int verde;
        int azul;
        if (fracao < 0.5) {
            double f = fracao / 0.5;
            vermelho = 0;
            verde = (int) Math.round(255 * f);
            azul = (int) Math.round(255 * (1 - f));
        } else {
            double f = (fracao - 0.5) / 0.5;
            vermelho = (int) Math.round(255 * f);
            verde = (int) Math.round(255 * (1 - f));
            azul = 0;
        }
        return new Color(vermelho, verde, azul, alfa);
    }
}
