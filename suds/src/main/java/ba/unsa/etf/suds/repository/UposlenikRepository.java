package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UposlenikRepository {
    private final DatabaseManager dbManager;

    public UposlenikRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<UposlenikDTO> findAllUposlenici() {
        List<UposlenikDTO> uposlenici = new ArrayList<>();
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
                s.IME_STANICE
            FROM NBP_USER u
            JOIN NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            JOIN STANICE s ON p.STANICA_ID = s.STANICA_ID
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                uposlenici.add(mapRowToDTO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju svih uposlenika", e);
        }
        return uposlenici;
    }

    public Optional<UposlenikDTO> findByUserId(Long userId) {
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
                s.IME_STANICE
            FROM NBP_USER u
            JOIN NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            JOIN STANICE s ON p.STANICA_ID = s.STANICA_ID
            WHERE u.ID = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju uposlenika po ID-u", e);
        }
        return Optional.empty();
    }

    // Pomoćna metoda da ne ponavljamo kod za mapiranje
    private UposlenikDTO mapRowToDTO(ResultSet rs) throws SQLException {
        return new UposlenikDTO(
                rs.getLong("USER_ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("USERNAME"),
                rs.getString("ULOGA"),
                rs.getString("BROJ_ZNACKE"),
                rs.getString("IME_STANICE")
        );
    }
}