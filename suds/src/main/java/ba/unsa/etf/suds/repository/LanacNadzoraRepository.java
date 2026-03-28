package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.LanacNadzora;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LanacNadzoraRepository {

    private final DatabaseManager databaseManager;

    public LanacNadzoraRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public LanacNadzora save(LanacNadzora lanac) {
        String sql = "INSERT INTO Lanac_Nadzora (dokaz_id, stanica_id, datum_primopredaje, predao_user_id, preuzeo_user_id, svrha_primopredaje, potvrda_status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, lanac.getDokazId());
            stmt.setLong(2, lanac.getStanicaId());
            stmt.setTimestamp(3, lanac.getDatumPrimopredaje() != null ? lanac.getDatumPrimopredaje() : new Timestamp(System.currentTimeMillis()));

            if (lanac.getPredaoUserId() != null) {
                stmt.setLong(4, lanac.getPredaoUserId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setLong(5, lanac.getPreuzeoUserId());
            stmt.setString(6, lanac.getSvrhaPrimopredaje());
            stmt.setString(7, lanac.getPotvrdaStatus() != null ? lanac.getPotvrdaStatus() : "Čeka potvrdu");

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lanac.setUnosId(rs.getLong(1));
            }
            return lanac;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri spašavanju lanca nadzora", e);
        }
    }

    public List<LanacNadzora> findByDokazId(Long dokazId) {
        List<LanacNadzora> lanacList = new ArrayList<>();
        String sql = "SELECT * FROM Lanac_Nadzora WHERE dokaz_id = ? ORDER BY datum_primopredaje ASC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dokazId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lanacList.add(new LanacNadzora(
                        rs.getLong("unos_id"), rs.getLong("dokaz_id"), rs.getLong("stanica_id"),
                        rs.getTimestamp("datum_primopredaje"), rs.getLong("predao_user_id"),
                        rs.getLong("preuzeo_user_id"), rs.getString("svrha_primopredaje"),
                        rs.getString("potvrda_status"), rs.getString("potvrda_napomena"),
                        rs.getTimestamp("potvrda_datum"), rs.getLong("potvrdio_user_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju lanca nadzora", e);
        }
        return lanacList;
    }
}