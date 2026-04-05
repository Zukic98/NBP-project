package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.LanacNadzora;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<LanacNadzora> findById(Long unosId) {
        String sql = "SELECT * FROM Lanac_Nadzora WHERE UNOS_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, unosId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToLanacNadzora(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching chain entry by ID: " + unosId, e);
        }
        return Optional.empty();
    }

    public List<LanacNadzora> findZahtjeviZaKorisnika(Long userId) {
        List<LanacNadzora> zahtjevi = new ArrayList<>();
        String sql = "SELECT * FROM Lanac_Nadzora WHERE PREUZEO_USER_ID = ? AND POTVRDA_STATUS = 'Čeka potvrdu' " +
                     "ORDER BY DATUM_PRIMOPREDAJE DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                zahtjevi.add(mapRowToLanacNadzora(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching pending handover requests for userId: " + userId, e);
        }
        return zahtjevi;
    }

    public void prihvati(Long unosId, Long potvrdioUserId) {
        String sql = "UPDATE Lanac_Nadzora SET POTVRDA_STATUS = 'Potvrđeno', POTVRDA_DATUM = ?, POTVRDIO_USER_ID = ? " +
                     "WHERE UNOS_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(2, potvrdioUserId);
            stmt.setLong(3, unosId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while accepting chain entry: " + unosId, e);
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
                lanacList.add(mapRowToLanacNadzora(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju lanca nadzora", e);
        }
        return lanacList;
    }

    private LanacNadzora mapRowToLanacNadzora(ResultSet rs) throws SQLException {
        LanacNadzora lanac = new LanacNadzora();
        lanac.setUnosId(rs.getLong("UNOS_ID"));
        lanac.setDokazId(rs.getLong("DOKAZ_ID"));
        lanac.setStanicaId(rs.getLong("STANICA_ID"));
        lanac.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
        
        long predaoUserId = rs.getLong("PREDAO_USER_ID");
        lanac.setPredaoUserId(rs.wasNull() ? null : predaoUserId);
        
        lanac.setPreuzeoUserId(rs.getLong("PREUZEO_USER_ID"));
        lanac.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
        lanac.setPotvrdaStatus(rs.getString("POTVRDA_STATUS"));
        lanac.setPotvrdaNapomena(rs.getString("POTVRDA_NAPOMENA"));
        lanac.setPotvrdaDatum(rs.getTimestamp("POTVRDA_DATUM"));
        
        long potvrdioUserId = rs.getLong("POTVRDIO_USER_ID");
        lanac.setPotvrdioUserId(rs.wasNull() ? null : potvrdioUserId);
        
        return lanac;
    }

}