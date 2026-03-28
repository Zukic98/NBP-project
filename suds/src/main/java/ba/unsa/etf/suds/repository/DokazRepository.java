package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Dokaz;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class DokazRepository {

    private final DatabaseManager databaseManager;

    // Spring automatski injekta tvoj DatabaseManager
    public DokazRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Dokaz save(Dokaz dokaz) {
        String sql = "INSERT INTO Dokazi (slucaj_id, stanica_id, opis, lokacija_pronalaska, tip_dokaza, status, datum_prikupa, prikupio_user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
}