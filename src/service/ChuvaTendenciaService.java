package service;

import java.util.List;

public class ChuvaTendenciaService {

    public double inclinacao(List<Double> valores) {
        int n = valores.size();
        if (n < 2) {
            return 0.0;
        }
        double somaX = 0, somaY = 0, somaXY = 0, somaXX = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = valores.get(i);
            somaX += x;
            somaY += y;
            somaXY += x * y;
            somaXX += x * x;
        }
        double denominador = n * somaXX - somaX * somaX;
        if (denominador == 0) {
            return 0.0;
        }
        return (n * somaXY - somaX * somaY) / denominador;
    }
}
