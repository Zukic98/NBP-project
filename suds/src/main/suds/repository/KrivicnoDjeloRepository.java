package ba.unsa.etf.suds.ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.KrivicnoDjelo;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class KrivicnoDjeloRepository {
    private final DatabaseManager dbManager;

    public KrivicnoDjeloRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<KrivicnoDjelo> findAll() {
        List<KrivicnoDjelo> djela = new ArrayList<>();
        String sql = "SELECT * FROM KRIVICNA_DJELA";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                djela.add(new KrivicnoDjelo(
                        rs.getLong("DJELO_ID"),
                        rs.getString("NAZIV"),
                        rs.getString("KATEGORIJA"),
                        rs.getString("KAZNENI_ZAKON_CLAN")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju krivičnih djela", e);
        }
        return djela;
    }

    public Optional<KrivicnoDjelo> findById(Long id) {
        // Popravljeno: u bazi se kolona zove DJELO_ID, a ne id
        String sql = "SELECT * FROM KRIVICNA_DJELA WHERE DJELO_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new KrivicnoDjelo(
                            rs.getLong("DJELO_ID"),
                            rs.getString("NAZIV"),
                            rs.getString("KATEGORIJA"),
                            rs.getString("KAZNENI_ZAKON_CLAN")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju djela po ID-u", e);
        }
        return Optional.empty();
    }
}