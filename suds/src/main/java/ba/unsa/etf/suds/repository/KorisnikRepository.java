package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Korisnik;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class KorisnikRepository {
    private final DatabaseManager dbManager;

    public KorisnikRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Korisnik> findAll() {
        List<Korisnik> korisnici = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, username FROM nbp.nbp_user";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Korisnik k = new Korisnik();
                k.setId(rs.getLong("id"));
                k.setFirstName(rs.getString("first_name"));
                k.setLastName(rs.getString("last_name"));
                k.setEmail(rs.getString("email"));
                k.setUsername(rs.getString("username"));
                korisnici.add(k);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri čitanju korisnika iz nbp.nbp_user", e);
        }
        return korisnici;
    }
}