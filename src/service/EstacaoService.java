package service;

import dao.EstacaoDAO;
import dao.MedicaoDAO;
import model.Estacao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EstacaoService {

    private final EstacaoDAO estacaoDAO = new EstacaoDAO();
    private final MedicaoDAO medicaoDAO = new MedicaoDAO();
    private final AlagamentoService alagamentoService = new AlagamentoService();

    public List<Estacao> carregar(LocalDate inicio, LocalDate fim, double limiarMm24) {
        List<Estacao> estacoes = estacaoDAO.listarComEstatisticas(inicio, fim);
        Map<String, double[]> acumulados = medicaoDAO.acumuladosChuva(fim);

        for (Estacao e : estacoes) {
            double[] acc = acumulados.get(e.getId());
            if (acc != null) {
                e.setMm24(acc[0]);
                e.setMm48(acc[1]);
                e.setMm72(acc[2]);
                e.setNivelAlerta(alagamentoService.classificarComLimiar(acc[0], acc[1], acc[2], limiarMm24));
            }
        }
        return estacoes;
    }

    public List<String> cidades() {
        return estacaoDAO.listarCidades();
    }

    public List<String> ids() {
        return estacaoDAO.listarIds();
    }
}
