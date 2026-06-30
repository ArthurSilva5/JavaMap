package dao;

import db.Conexao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicaoDAO {

    public Map<String, double[]> acumuladosChuva(LocalDate fim) {
        LocalDate ini48 = fim.minusDays(1);
        LocalDate ini72 = fim.minusDays(2);
        String sql =
                "SELECT station_id, "
              + "  SUM(CASE WHEN obs_date = ? THEN precip_total ELSE 0 END) AS mm24, "
              + "  SUM(CASE WHEN obs_date BETWEEN ? AND ? THEN precip_total ELSE 0 END) AS mm48, "
              + "  SUM(precip_total) AS mm72 "
              + "FROM history_daily "
              + "WHERE obs_date BETWEEN ? AND ? "
              + "GROUP BY station_id";

        Map<String, double[]> mapa = new HashMap<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fim.toString());
            ps.setString(2, ini48.toString());
            ps.setString(3, fim.toString());
            ps.setString(4, ini72.toString());
            ps.setString(5, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double mm24 = valor(rs.getBigDecimal("mm24"));
                    double mm48 = valor(rs.getBigDecimal("mm48"));
                    double mm72 = valor(rs.getBigDecimal("mm72"));
                    mapa.put(rs.getString("station_id"), new double[]{mm24, mm48, mm72});
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao calcular acumulados de chuva: " + ex.getMessage(), ex);
        }
        return mapa;
    }

    public List<double[]> serieTemperatura(String stationId, LocalDate inicio, LocalDate fim) {
        String sql =
                "SELECT temp_low, temp_avg, temp_high "
              + "FROM history_daily "
              + "WHERE station_id = ? AND obs_date BETWEEN ? AND ? "
              + "ORDER BY obs_date";
        List<double[]> serie = new ArrayList<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            ps.setString(2, inicio.toString());
            ps.setString(3, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    serie.add(new double[]{
                            valor(rs.getBigDecimal("temp_low")),
                            valor(rs.getBigDecimal("temp_avg")),
                            valor(rs.getBigDecimal("temp_high"))
                    });
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar série de temperatura: " + ex.getMessage(), ex);
        }
        return serie;
    }

    public Map<Integer, double[]> climatologiaMensal(String stationId) {
        String sql =
                "SELECT MONTH(obs_date) AS mes, AVG(temp_high) AS media_max, AVG(temp_low) AS media_min "
              + "FROM history_daily WHERE station_id = ? GROUP BY MONTH(obs_date)";
        Map<Integer, double[]> mapa = new HashMap<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getInt("mes"), new double[]{
                            valor(rs.getBigDecimal("media_max")),
                            valor(rs.getBigDecimal("media_min"))
                    });
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao calcular climatologia: " + ex.getMessage(), ex);
        }
        return mapa;
    }

    public List<double[]> serieMaxMinComMes(String stationId, LocalDate inicio, LocalDate fim) {
        String sql =
                "SELECT MONTH(obs_date) AS mes, temp_high, temp_low "
              + "FROM history_daily "
              + "WHERE station_id = ? AND obs_date BETWEEN ? AND ? "
              + "ORDER BY obs_date";
        List<double[]> serie = new ArrayList<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stationId);
            ps.setString(2, inicio.toString());
            ps.setString(3, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    serie.add(new double[]{
                            rs.getInt("mes"),
                            valor(rs.getBigDecimal("temp_high")),
                            valor(rs.getBigDecimal("temp_low"))
                    });
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar série max/min: " + ex.getMessage(), ex);
        }
        return serie;
    }

    private static double valor(BigDecimal v) {
        return v == null ? 0.0 : v.doubleValue();
    }
}
