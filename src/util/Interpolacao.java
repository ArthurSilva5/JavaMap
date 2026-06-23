package util;

import java.util.List;

public class Interpolacao {

    public static double idw(double latitude, double longitude, List<double[]> pontos, double potencia) {
        double somaPesos = 0.0;
        double somaValores = 0.0;
        for (double[] p : pontos) {
            double dLat = p[0] - latitude;
            double dLon = p[1] - longitude;
            double distancia2 = dLat * dLat + dLon * dLon;
            if (distancia2 < 1e-9) {
                return p[2];
            }
            double peso = 1.0 / Math.pow(distancia2, potencia / 2.0);
            somaPesos += peso;
            somaValores += peso * p[2];
        }
        if (somaPesos == 0.0) {
            return Double.NaN;
        }
        return somaValores / somaPesos;
    }
}
