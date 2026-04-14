package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.NbpUser;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
public class NbpUserRepository {
    private final DatabaseManager dbManager;

    public NbpUserRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Optional<NbpUser> findByUsername(String username) {
        String sql = "SELECT * FROM nbp.NBP_USER WHERE USERNAME = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToNbpUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching user by username: " + username, e);
        }
        return Optional.empty();
    }

    public Optional<NbpUser> findById(Long id) {
        String sql = "SELECT * FROM nbp.NBP_USER WHERE ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToNbpUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching user by ID: " + id, e);
        }
        return Optional.empty();
    }

    private NbpUser mapRowToNbpUser(ResultSet rs) throws SQLException {
        return new NbpUser(
                rs.getLong("ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"),
                rs.getString("USERNAME"),
                rs.getString("PHONE_NUMBER"),
                rs.getDate("BIRTH_DATE"),
                rs.getLong("ADDRESS_ID"),
                rs.getLong("ROLE_ID")
        );
    }
}
