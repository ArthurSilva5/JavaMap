package dao;

import db.Conexao;
import model.Estacao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EstacaoDAO {

    public List<Estacao> listarComEstatisticas(LocalDate inicio, LocalDate fim) {
        String sql =
                "SELECT s.station_id, s.station_name, s.latitude, s.longitude, s.qc_status_label, "
              + "       AVG(d.temp_avg) AS temp_media, "
              + "       MIN(d.temp_low) AS temp_min, "
              + "       MAX(d.temp_high) AS temp_max, "
              + "       AVG(d.humidity_avg) AS umidade_media, "
              + "       SUM(d.precip_total) AS precip_total "
              + "FROM stations s "
              + "LEFT JOIN history_daily d "
              + "       ON d.station_id = s.station_id "
              + "      AND d.obs_date BETWEEN ? AND ? "
              + "GROUP BY s.station_id, s.station_name, s.latitude, s.longitude, s.qc_status_label "
              + "ORDER BY s.station_name, s.station_id";

        List<Estacao> lista = new ArrayList<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Estacao e = new Estacao();
                    e.setId(rs.getString("station_id"));
                    e.setNome(rs.getString("station_name"));
                    e.setLatitude(rs.getDouble("latitude"));
                    e.setLongitude(rs.getDouble("longitude"));
                    e.setQcStatus(rs.getString("qc_status_label"));
                    e.setTemperaturaMedia(toDouble(rs.getBigDecimal("temp_media")));
                    e.setTemperaturaMin(toDouble(rs.getBigDecimal("temp_min")));
                    e.setTemperaturaMax(toDouble(rs.getBigDecimal("temp_max")));
                    e.setUmidadeMedia(toDouble(rs.getBigDecimal("umidade_media")));
                    e.setPrecipitacaoTotal(toDouble(rs.getBigDecimal("precip_total")));
                    lista.add(e);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar estações: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public List<String> listarCidades() {
        String sql = "SELECT DISTINCT station_name FROM stations ORDER BY station_name";
        return consultarTextos(sql);
    }

    public List<String> listarIds() {
        String sql = "SELECT station_id FROM stations ORDER BY station_id";
        return consultarTextos(sql);
    }

    private List<String> consultarTextos(String sql) {
        List<String> lista = new ArrayList<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao consultar lista: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private static Double toDouble(BigDecimal valor) {
        return valor == null ? null : valor.doubleValue();
    }
}
