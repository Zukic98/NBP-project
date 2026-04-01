package ba.unsa.etf.suds.ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Stanica;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StanicaRepository {
    private final DatabaseManager dbManager;

    public StanicaRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Stanica> findAll() {
        List<Stanica> stanice = new ArrayList<>();
        String sql = "SELECT * FROM STANICE";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stanice.add(new Stanica(
                        rs.getLong("STANICA_ID"),
                        rs.getString("IME_STANICE"),
                        rs.getLong("ADRESA_ID"),
                        rs.getTimestamp("DATUM_KREIRANJA")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju stanica", e);
        }
        return stanice;
    }

    public Optional<Stanica> findById(Long id) {
        String sql = "SELECT * FROM STANICE WHERE STANICA_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Stanica(
                            rs.getLong("STANICA_ID"),
                            rs.getString("IME_STANICE"),
                            rs.getLong("ADRESA_ID"),
                            rs.getTimestamp("DATUM_KREIRANJA")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju stanice po ID-u", e);
        }
        return Optional.empty();
    }
}