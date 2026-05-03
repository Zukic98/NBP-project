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

/**
 * Repozitorij za upravljanje lancima nadzora (primopredajama dokaza) iz tabele {@code LANAC_NADZORA}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class LanacNadzoraRepository {

    private final DatabaseManager databaseManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public LanacNadzoraRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Sprema novi unos primopredaje u tabelu {@code LANAC_NADZORA} i vraća entitet sa generisanim ID-om.
     *
     * @param lanac entitet primopredaje koji se sprema
     * @return sačuvani entitet sa postavljenim {@code unosId}-om
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata unos primopredaje po primarnom ključu.
     *
     * @param unosId identifikator unosa ({@code UNOS_ID})
     * @return {@link Optional} sa pronađenim unosom, ili prazan ako ne postoji
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata sve primopredaje koje čekaju potvrdu od strane datog korisnika.
     *
     * @param userId identifikator korisnika koji preuzima dokaz
     * @return lista primopredaja sa statusom "Čeka potvrdu"
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Prihvata primopredaju postavljanjem statusa na "Potvrđeno" i bilježenjem datuma i korisnika potvrde.
     *
     * @param unosId       identifikator unosa koji se prihvata
     * @param potvrdioUserId identifikator korisnika koji potvrđuje primopredaju
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata primopredaje koje čekaju potvrdu od strane datog korisnika, sa JOIN-om na dokaze i korisnike.
     *
     * @param userId identifikator korisnika koji preuzima dokaz
     * @return lista DTO-ova sa detaljima primopredaja na čekanju
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata primopredaje koje je dati korisnik poslao, a koje još čekaju potvrdu.
     *
     * @param userId identifikator korisnika koji je predao dokaz
     * @return lista DTO-ova sa detaljima poslatih primopredaja na čekanju
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Ažurira status primopredaje (potvrda ili odbijanje) zajedno sa napomenom, datumom i korisnikom koji je odlučio.
     *
     * @param unosId         identifikator unosa koji se ažurira
     * @param status         novi status (npr. "Potvrđeno" ili "Odbijeno")
     * @param napomena       napomena uz odluku
     * @param potvrdioUserId identifikator korisnika koji donosi odluku
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Poništava primopredaju postavljanjem statusa na "Poništeno" uz razlog i korisnika koji poništava.
     *
     * @param unosId identifikator unosa koji se poništava
     * @param razlog razlog poništavanja
     * @param userId identifikator korisnika koji poništava
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata sve unose lanca nadzora za dati dokaz, sortirane po datumu primopredaje.
     *
     * @param dokazId identifikator dokaza
     * @return lista unosa lanca nadzora za dati dokaz
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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
