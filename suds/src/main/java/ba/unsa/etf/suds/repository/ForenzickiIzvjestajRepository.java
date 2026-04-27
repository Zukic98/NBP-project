package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class ForenzickiIzvjestajRepository {

    private final DatabaseManager databaseManager;

    public ForenzickiIzvjestajRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

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