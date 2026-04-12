package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.MojaPrimopredajaDTO;
import ba.unsa.etf.suds.dto.PrimopredajaZaPotvrduDTO;
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
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"UNOS_ID"})) {

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

    public List<PrimopredajaZaPotvrduDTO> findPrimopredajeZaPotvrdu(Long userId) {
        List<PrimopredajaZaPotvrduDTO> rezultat = new ArrayList<>();
        String sql = "SELECT ln.UNOS_ID, d.OPIS AS DOKAZ_OPIS, d.TIP_DOKAZA, " +
                "(u.FIRST_NAME||' '||u.LAST_NAME) AS PREDAO_IME, ln.SVRHA_PRIMOPREDAJE, " +
                "ln.DATUM_PRIMOPREDAJE, ln.DOKAZ_ID " +
                "FROM LANAC_NADZORA ln " +
                "JOIN DOKAZI d ON ln.DOKAZ_ID=d.DOKAZ_ID " +
                "JOIN nbp.NBP_USER u ON ln.PREDAO_USER_ID=u.ID " +
                "WHERE ln.PREUZEO_USER_ID=? AND ln.POTVRDA_STATUS='Čeka potvrdu' " +
                "ORDER BY ln.DATUM_PRIMOPREDAJE DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rezultat.add(mapRowToPrimopredajaZaPotvrduDto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching pending approvals for userId: " + userId, e);
        }
        return rezultat;
    }

    public List<MojaPrimopredajaDTO> findMojaSlanjaNaPotvrdi(Long userId) {
        List<MojaPrimopredajaDTO> rezultat = new ArrayList<>();
        String sql = "SELECT ln.UNOS_ID, d.OPIS AS DOKAZ_OPIS, d.TIP_DOKAZA, " +
                "(u.FIRST_NAME||' '||u.LAST_NAME) AS PREUZEO_IME, ln.SVRHA_PRIMOPREDAJE, " +
                "ln.DATUM_PRIMOPREDAJE, ln.DOKAZ_ID " +
                "FROM LANAC_NADZORA ln " +
                "JOIN DOKAZI d ON ln.DOKAZ_ID=d.DOKAZ_ID " +
                "JOIN nbp.NBP_USER u ON ln.PREUZEO_USER_ID=u.ID " +
                "WHERE ln.PREDAO_USER_ID=? AND ln.POTVRDA_STATUS='Čeka potvrdu' " +
                "ORDER BY ln.DATUM_PRIMOPREDAJE DESC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rezultat.add(mapRowToMojaPrimopredajaDto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching sent pending handovers for userId: " + userId, e);
        }
        return rezultat;
    }

    public void potvrdiIliOdbij(Long unosId, String status, String napomena, Long potvrdioUserId) {
        String sql = "UPDATE LANAC_NADZORA SET POTVRDA_STATUS=?, POTVRDA_NAPOMENA=?, POTVRDA_DATUM=?, POTVRDIO_USER_ID=? WHERE UNOS_ID=?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, napomena);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(4, potvrdioUserId);
            stmt.setLong(5, unosId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating handover confirmation for entry: " + unosId, e);
        }
    }

    public void ponisti(Long unosId, String razlog, Long userId) {
        String sql = "UPDATE LANAC_NADZORA SET POTVRDA_STATUS='Poništeno', POTVRDA_NAPOMENA=?, POTVRDA_DATUM=?, POTVRDIO_USER_ID=? WHERE UNOS_ID=?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, razlog);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(3, userId);
            stmt.setLong(4, unosId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while cancelling handover entry: " + unosId, e);
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

    private PrimopredajaZaPotvrduDTO mapRowToPrimopredajaZaPotvrduDto(ResultSet rs) throws SQLException {
        PrimopredajaZaPotvrduDTO dto = new PrimopredajaZaPotvrduDTO();
        dto.setUnosId(rs.getLong("UNOS_ID"));
        dto.setDokazOpis(rs.getString("DOKAZ_OPIS"));
        dto.setTipDokaza(rs.getString("TIP_DOKAZA"));
        dto.setPredaoIme(rs.getString("PREDAO_IME"));
        dto.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
        dto.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
        dto.setDokazId(rs.getLong("DOKAZ_ID"));
        return dto;
    }

    private MojaPrimopredajaDTO mapRowToMojaPrimopredajaDto(ResultSet rs) throws SQLException {
        MojaPrimopredajaDTO dto = new MojaPrimopredajaDTO();
        dto.setUnosId(rs.getLong("UNOS_ID"));
        dto.setDokazOpis(rs.getString("DOKAZ_OPIS"));
        dto.setTipDokaza(rs.getString("TIP_DOKAZA"));
        dto.setPreuzeoIme(rs.getString("PREUZEO_IME"));
        dto.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
        dto.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
        dto.setDokazId(rs.getLong("DOKAZ_ID"));
        return dto;
    }

}
