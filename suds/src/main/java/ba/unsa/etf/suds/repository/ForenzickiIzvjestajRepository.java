package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import org.springframework.stereotype.Repository;

import java.sql.*;

/**
 * Repozitorij za rad sa tabelom FORENZICKI_IZVJESTAJI (forenzički izvještaji).
 *
 * <p>Koristi čisti JDBC bez ORM-a (pravilo predmeta NBP). Svaka metoda
 * otvara vlastitu konekciju preko {@link DatabaseManager#getConnection()}
 * i zatvara je kroz try-with-resources. SQLException-i se wrappaju
 * u RuntimeException sa engleskom porukom.
 */
@Repository
public class ForenzickiIzvjestajRepository {

    private final DatabaseManager databaseManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public ForenzickiIzvjestajRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Dohvata zaključak najnovijeg forenzičkog izvještaja za dati dokaz.
     *
     * @param dokazId ID dokaza
     * @return tekst zaključka, ili {@code null} ako nema izvještaja
     * @throws RuntimeException ako dođe do SQL greške
     */
    public String findZakljucakByDokazId(Long dokazId) {
        String sql = "SELECT zakljucak FROM Forenzicki_Izvjestaji WHERE dokaz_id = ? ORDER BY datum_kreiranja DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dokazId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("zakljucak");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju zaključka", e);
        }
    }

    /**
     * Dohvata kompletan forenzički izvještaj za dati dokaz (najnoviji po datumu kreiranja).
     *
     * @param dokazId ID dokaza
     * @return objekat izvještaja ako postoji, ili {@code null}
     * @throws RuntimeException ako dođe do SQL greške
     */
    public ForenzickiIzvjestaj findByDokazId(Long dokazId) {
        String sql = "SELECT izvjestaj_id, dokaz_id, kreator_user_id, sadrzaj, zakljucak, datum_kreiranja " +
                     "FROM Forenzicki_Izvjestaji WHERE dokaz_id = ? ORDER BY datum_kreiranja DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dokazId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ForenzickiIzvjestaj izvjestaj = new ForenzickiIzvjestaj();
                izvjestaj.setIzvjestajId(rs.getLong("izvjestaj_id"));
                izvjestaj.setDokazId(rs.getLong("dokaz_id"));
                izvjestaj.setKreatorUserId(rs.getLong("kreator_user_id"));
                izvjestaj.setSadrzaj(rs.getString("sadrzaj"));
                izvjestaj.setZakljucak(rs.getString("zakljucak"));
                
                izvjestaj.setDatumKreiranja(rs.getTimestamp("datum_kreiranja"));
                
                return izvjestaj;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju kompletnog izvještaja", e);
        }
    }

    /**
     * Sprema novi forenzički izvještaj u tabelu FORENZICKI_IZVJESTAJI.
     * Automatski postavlja {@code datumKreiranja} na trenutno vrijeme.
     *
     * @param izvjestaj objekat izvještaja koji se sprema
     * @return isti objekat sa postavljenim {@code datumKreiranja}
     * @throws RuntimeException ako dođe do SQL greške
     */
    public ForenzickiIzvjestaj save(ForenzickiIzvjestaj izvjestaj) {
        String sql = "INSERT INTO Forenzicki_Izvjestaji (dokaz_id, kreator_user_id, sadrzaj, zakljucak, datum_kreiranja) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp sada = new Timestamp(System.currentTimeMillis());
            izvjestaj.setDatumKreiranja(sada);

            stmt.setLong(1, izvjestaj.getDokazId());
            stmt.setLong(2, izvjestaj.getKreatorUserId());
            stmt.setString(3, izvjestaj.getSadrzaj());
            stmt.setString(4, izvjestaj.getZakljucak());
            stmt.setTimestamp(5, sada);

            stmt.executeUpdate();
            return izvjestaj;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri spašavanju", e);
        }
    }

    /**
     * Ažurira sadržaj i zaključak postojećeg forenzičkog izvještaja.
     *
     * @param izvjestaj objekat sa postavljenim {@code izvjestajId}, {@code sadrzaj} i {@code zakljucak}
     * @return isti objekat nakon ažuriranja
     * @throws RuntimeException ako dođe do SQL greške
     */
    public ForenzickiIzvjestaj update(ForenzickiIzvjestaj izvjestaj) {
    String sql = "UPDATE Forenzicki_Izvjestaji SET sadrzaj = ?, zakljucak = ? WHERE izvjestaj_id = ?";

    try (Connection conn = databaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, izvjestaj.getSadrzaj());
        stmt.setString(2, izvjestaj.getZakljucak());
        stmt.setLong(3, izvjestaj.getIzvjestajId());

        stmt.executeUpdate();
        return izvjestaj;
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri ažuriranju izvještaja", e);
    }
}
}