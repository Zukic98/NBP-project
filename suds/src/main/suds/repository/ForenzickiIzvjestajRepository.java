package ba.unsa.etf.suds.ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
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
            throw new RuntimeException("Greška pri dohvatanju forenzičkog izvještaja", e);
        }
    }
}