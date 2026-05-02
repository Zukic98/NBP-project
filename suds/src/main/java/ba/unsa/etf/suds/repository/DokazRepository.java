package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DokazListDTO;
import ba.unsa.etf.suds.dto.LanacDetaljiDTO;
import ba.unsa.etf.suds.model.Dokaz;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij za rad sa tabelom DOKAZI (forenzički dokazi).
 *
 * <p>Koristi čisti JDBC bez ORM-a (pravilo predmeta NBP). Svaka metoda
 * otvara vlastitu konekciju preko {@link DatabaseManager#getConnection()}
 * i zatvara je kroz try-with-resources. SQLException-i se wrappaju
 * u RuntimeException sa engleskom porukom.
 *
 * <p>Neke metode vrše JOIN sa tabelama LANAC_NADZORA i {@code nbp.NBP_USER}
 * radi dohvatanja proširenih informacija o dokazima.
 */
@Repository
public class DokazRepository {

    private final DatabaseManager databaseManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    // Spring automatski injekta tvoj DatabaseManager
    public DokazRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Sprema novi dokaz u tabelu DOKAZI i vraća ga sa generisanim ID-om.
     *
     * @param dokaz objekat dokaza koji se sprema
     * @return isti objekat sa postavljenim {@code dokazId}
     * @throws RuntimeException ako dođe do SQL greške
     */
    public Dokaz save(Dokaz dokaz) {
        String sql = "INSERT INTO Dokazi (slucaj_id, stanica_id, opis, lokacija_pronalaska, tip_dokaza, status, datum_prikupa, prikupio_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"DOKAZ_ID"})) {

            stmt.setLong(1, dokaz.getSlucajId());
            stmt.setLong(2, dokaz.getStanicaId());
            stmt.setString(3, dokaz.getOpis());
            stmt.setString(4, dokaz.getLokacijaPronalaska());
            stmt.setString(5, dokaz.getTipDokaza());
            stmt.setString(6, dokaz.getStatus() != null ? dokaz.getStatus() : "Odobren");
            stmt.setTimestamp(7, dokaz.getDatumPrikupa());
            stmt.setLong(8, dokaz.getPrikupioUserId());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                dokaz.setDokazId(rs.getLong(1));
            }
            return dokaz;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri spašavanju dokaza", e);
        }
    }

    /**
     * Učitava listu dokaza za dati slučaj iz tabele DOKAZI.
     * Vrši LEFT JOIN sa {@code nbp.NBP_USER} radi dohvatanja imena prikupljača.
     *
     * @param slucajId ID slučaja čiji se dokazi traže
     * @return lista DTO objekata, sortirana po datumu prikupljanja silazno
     * @throws RuntimeException ako dođe do SQL greške
     */
    public List<DokazListDTO> findBySlucajId(Long slucajId) {
        String sql = "SELECT d.DOKAZ_ID, d.OPIS, d.LOKACIJA_PRONALASKA, d.TIP_DOKAZA, d.STATUS, " +
                "(u.FIRST_NAME||' '||u.LAST_NAME) AS PRIKUPIO_IME, d.DATUM_PRIKUPA, d.SLUCAJ_ID " +
                "FROM DOKAZI d " +
                "LEFT JOIN nbp.NBP_USER u ON d.PRIKUPIO_USER_ID = u.ID " +
                "WHERE d.SLUCAJ_ID = ? " +
                "ORDER BY d.DATUM_PRIKUPA DESC";

        List<DokazListDTO> rezultat = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DokazListDTO dto = new DokazListDTO();
                    dto.setDokazId(rs.getLong("DOKAZ_ID"));
                    dto.setOpis(rs.getString("OPIS"));
                    dto.setLokacijaPronalaska(rs.getString("LOKACIJA_PRONALASKA"));
                    dto.setTipDokaza(rs.getString("TIP_DOKAZA"));
                    dto.setStatus(rs.getString("STATUS"));
                    dto.setPrikupioIme(rs.getString("PRIKUPIO_IME"));
                    dto.setDatumPrikupa(rs.getTimestamp("DATUM_PRIKUPA"));
                    dto.setSlucajId(rs.getLong("SLUCAJ_ID"));
                    rezultat.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching evidence list by case ID: " + slucajId, e);
        }

        return rezultat;
    }

    /**
     * Dohvata trenutno stanje dokaza: ko ga drži, status i da li postoji čekajuća potvrda.
     * Izvršava tri SQL upita unutar iste konekcije: osnovni podaci, zadnja potvrđena primopredaja
     * i provjera čekajućih potvrda.
     *
     * @param dokazId ID dokaza
     * @return Optional sa {@link DokazStanjeInfo} ako dokaz postoji, inače prazan
     * @throws RuntimeException ako dođe do SQL greške
     */
    public Optional<DokazStanjeInfo> findStanje(Long dokazId) {
        String dokazSql = "SELECT DOKAZ_ID, STATUS, PRIKUPIO_USER_ID FROM DOKAZI WHERE DOKAZ_ID = ?";
        String confirmedSql = "SELECT PREUZEO_USER_ID, DATUM_PRIMOPREDAJE " +
                "FROM LANAC_NADZORA WHERE DOKAZ_ID = ? AND POTVRDA_STATUS = 'Potvrđeno' " +
                "ORDER BY DATUM_PRIMOPREDAJE DESC FETCH FIRST 1 ROW ONLY";
        String pendingSql = "SELECT COUNT(*) FROM LANAC_NADZORA WHERE DOKAZ_ID = ? AND POTVRDA_STATUS = 'Čeka potvrdu'";

        try (Connection conn = databaseManager.getConnection()) {
            Long prikupioUserId;
            String status;

            try (PreparedStatement dokazStmt = conn.prepareStatement(dokazSql)) {
                dokazStmt.setLong(1, dokazId);
                try (ResultSet rs = dokazStmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    prikupioUserId = rs.getLong("PRIKUPIO_USER_ID");
                    if (rs.wasNull()) {
                        prikupioUserId = null;
                    }
                    status = rs.getString("STATUS");
                }
            }

            Long trenutniNosilacId = prikupioUserId;
            Timestamp zadnjaPrimopredaja = null;

            try (PreparedStatement confirmedStmt = conn.prepareStatement(confirmedSql)) {
                confirmedStmt.setLong(1, dokazId);
                try (ResultSet rs = confirmedStmt.executeQuery()) {
                    if (rs.next()) {
                        long preuzeo = rs.getLong("PREUZEO_USER_ID");
                        trenutniNosilacId = rs.wasNull() ? null : preuzeo;
                        zadnjaPrimopredaja = rs.getTimestamp("DATUM_PRIMOPREDAJE");
                    }
                }
            }

            boolean imaCekajucuPotvrdu = false;
            try (PreparedStatement pendingStmt = conn.prepareStatement(pendingSql)) {
                pendingStmt.setLong(1, dokazId);
                try (ResultSet rs = pendingStmt.executeQuery()) {
                    if (rs.next()) {
                        imaCekajucuPotvrdu = rs.getInt(1) > 0;
                    }
                }
            }

            String efektivniStatus = status;
            if (imaCekajucuPotvrdu) {
                efektivniStatus = "Čeka potvrdu";
            }

            String imeNosioca = findImeByUserId(conn, trenutniNosilacId);

            DokazStanjeInfo stanjeInfo = new DokazStanjeInfo(
                    trenutniNosilacId,
                    imeNosioca,
                    efektivniStatus,
                    zadnjaPrimopredaja,
                    imaCekajucuPotvrdu
            );
            return Optional.of(stanjeInfo);
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching evidence state for ID: " + dokazId, e);
        }
    }

    /**
     * Učitava kompletan lanac nadzora za dati dokaz sa imenima svih učesnika.
     * Vrši JOIN sa {@code nbp.NBP_USER} za predavaoca, primaoca i potvrđivača.
     *
     * @param dokazId ID dokaza
     * @return lista DTO objekata lanca nadzora, sortirana po datumu primopredaje uzlazno
     * @throws RuntimeException ako dođe do SQL greške
     */
    public List<LanacDetaljiDTO> findLanacWithNames(Long dokazId) {
        String sql = "SELECT ln.UNOS_ID, ln.DATUM_PRIMOPREDAJE, " +
                "(u1.FIRST_NAME||' '||u1.LAST_NAME) AS PREDAO_IME, " +
                "(u2.FIRST_NAME||' '||u2.LAST_NAME) AS PREUZEO_IME, " +
                "ln.SVRHA_PRIMOPREDAJE, ln.POTVRDA_STATUS, ln.POTVRDA_NAPOMENA, ln.POTVRDA_DATUM, " +
                "(u3.FIRST_NAME||' '||u3.LAST_NAME) AS POTVRDIO_IME " +
                "FROM LANAC_NADZORA ln " +
                "LEFT JOIN nbp.NBP_USER u1 ON ln.PREDAO_USER_ID = u1.ID " +
                "LEFT JOIN nbp.NBP_USER u2 ON ln.PREUZEO_USER_ID = u2.ID " +
                "LEFT JOIN nbp.NBP_USER u3 ON ln.POTVRDIO_USER_ID = u3.ID " +
                "WHERE ln.DOKAZ_ID = ? " +
                "ORDER BY ln.DATUM_PRIMOPREDAJE ASC";

        List<LanacDetaljiDTO> lanac = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dokazId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LanacDetaljiDTO dto = new LanacDetaljiDTO();
                    dto.setUnosId(rs.getLong("UNOS_ID"));
                    dto.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
                    dto.setPredaoIme(rs.getString("PREDAO_IME"));
                    dto.setPreuzeoIme(rs.getString("PREUZEO_IME"));
                    dto.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
                    dto.setPotvrdaStatus(rs.getString("POTVRDA_STATUS"));
                    dto.setPotvrdaNapomena(rs.getString("POTVRDA_NAPOMENA"));
                    dto.setPotvrdaDatum(rs.getTimestamp("POTVRDA_DATUM"));
                    dto.setPotvrdioIme(rs.getString("POTVRDIO_IME"));
                    lanac.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching chain of custody for evidence ID: " + dokazId, e);
        }
        return lanac;
    }

    /**
     * Dohvata ID stanice kojoj pripada dati korisnik iz tabele UPOSLENIK_PROFIL.
     *
     * @param userId ID korisnika
     * @return ID stanice, ili {@code null} ako korisnik nema profil ili stanica nije postavljena
     * @throws RuntimeException ako dođe do SQL greške
     */
    public Long findStanicaIdByUserId(Long userId) {
        String sql = "SELECT STANICA_ID FROM UPOSLENIK_PROFIL WHERE USER_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long stanicaId = rs.getLong("STANICA_ID");
                    return rs.wasNull() ? null : stanicaId;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching station ID for user: " + userId, e);
        }
    }

    /**
     * Ažurira status dokaza u tabeli DOKAZI.
     *
     * @param dokazId    ID dokaza koji se ažurira
     * @param noviStatus novi status koji se postavlja
     * @return {@code true} ako je ažuriran barem jedan red, inače {@code false}
     * @throws RuntimeException ako dođe do SQL greške
     */
    public boolean updateStatus(Long dokazId, String noviStatus) {
        String sql = "UPDATE Dokazi SET STATUS = ? WHERE DOKAZ_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, noviStatus);
            stmt.setLong(2, dokazId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating evidence status for ID: " + dokazId, e);
        }
    }

    /**
     * Pretražuje dokaz po primarnom ključu u tabeli DOKAZI.
     *
     * @param id vrijednost DOKAZ_ID kolone
     * @return objekat dokaza ako postoji, ili {@code null}
     * @throws RuntimeException ako dođe do SQL greške
     */
    public Dokaz findById(Long id) {
        String sql = "SELECT * FROM Dokazi WHERE dokaz_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Dokaz(rs.getLong("dokaz_id"), rs.getLong("slucaj_id"), rs.getLong("stanica_id"),
                        rs.getString("opis"), rs.getString("lokacija_pronalaska"), rs.getString("tip_dokaza"),
                        rs.getString("status"), rs.getTimestamp("datum_prikupa"), rs.getLong("prikupio_user_id"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju dokaza", e);
        }
    }

    private String findImeByUserId(Connection conn, Long userId) throws SQLException {
        if (userId == null) {
            return null;
        }

        String sql = "SELECT FIRST_NAME, LAST_NAME FROM nbp.NBP_USER WHERE ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("FIRST_NAME");
                    String lastName = rs.getString("LAST_NAME");
                    if (firstName == null && lastName == null) {
                        return null;
                    }
                    return (firstName != null ? firstName : "") +
                            ((firstName != null && lastName != null) ? " " : "") +
                            (lastName != null ? lastName : "");
                }
            }
        }
        return null;
    }

    public record DokazStanjeInfo(
            Long trenutniNosilacId,
            String trenutniNosilacIme,
            String status,
            Timestamp zadnjaPrimopredaja,
            boolean imaCekajucuPotvrdu
    ) {
    }
}
