package service;

import dao.MedicaoDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class OndaService {

    private final MedicaoDAO medicaoDAO = new MedicaoDAO();

    public String diagnosticar(String stationId, LocalDate inicio, LocalDate fim) {
        Map<Integer, double[]> climatologia = medicaoDAO.climatologiaMensal(stationId);
        List<double[]> serie = medicaoDAO.serieMaxMinComMes(stationId, inicio, fim);

        int diasCalor = maiorSequencia(serie, climatologia, true);
        int diasFrio = maiorSequencia(serie, climatologia, false);

        StringBuilder sb = new StringBuilder();
        if (diasCalor >= 3) {
            sb.append("Onda de calor (").append(diasCalor).append(" dias)");
        }
        if (diasFrio >= 3) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append("Onda de frio (").append(diasFrio).append(" dias)");
        }
        if (sb.length() == 0) {
            sb.append("Sem ondas no período");
        }
        return sb.toString();
    }

    private int maiorSequencia(List<double[]> serie, Map<Integer, double[]> climatologia, boolean calor) {
        int maior = 0;
        int atual = 0;
        for (double[] dia : serie) {
            int mes = (int) dia[0];
            double maxima = dia[1];
            double minima = dia[2];
            double[] normal = climatologia.get(mes);
            if (normal == null) {
                atual = 0;
                continue;
            }
            boolean condicao = calor ? (maxima >= normal[0] + 5) : (minima <= normal[1] - 5);
            if (condicao) {
                atual++;
                if (atual > maior) {
                    maior = atual;
                }
            } else {
                atual = 0;
            }
        }
        return maior;
    }
}
