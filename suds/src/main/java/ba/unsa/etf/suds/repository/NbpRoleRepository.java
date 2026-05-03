package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.NbpRole;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

/**
 * Repozitorij za rad sa tabelom {@code nbp.NBP_ROLE} (uloge korisnika).
 *
 * <p>Koristi čisti JDBC bez ORM-a (pravilo predmeta NBP). Svaka metoda
 * otvara vlastitu konekciju preko {@link DatabaseManager#getConnection()}
 * i zatvara je kroz try-with-resources. SQLException-i se wrappaju
 * u RuntimeException sa engleskom porukom.
 */
@Repository
public class NbpRoleRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public NbpRoleRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Pretražuje ulogu po primarnom ključu u tabeli {@code nbp.NBP_ROLE}.
     *
     * @param id vrijednost ID kolone
     * @return Optional sa ulogom ako postoji, inače prazan
     * @throws RuntimeException ako dođe do SQL greške
     */
    public Optional<NbpRole> findById(Long id) {
        String sql = "SELECT * FROM nbp.NBP_ROLE WHERE ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new NbpRole(rs.getLong("ID"), rs.getString("NAME")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching role by ID: " + id, e);
        }
        return Optional.empty();
    }
}
