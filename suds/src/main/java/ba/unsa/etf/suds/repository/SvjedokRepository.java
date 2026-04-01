package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Svjedok;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SvjedokRepository {
    private final DatabaseManager dbManager;

    public SvjedokRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Svjedok> findAll() {
        List<Svjedok> svjedoci = new ArrayList<>();
        String sql = "SELECT * FROM Svjedoci";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                svjedoci.add(new Svjedok(
                        rs.getLong("svjedok_id"),
                        rs.getLong("slucaj_id"),
                        rs.getString("ime_prezime"),
                        rs.getString("jmbg"),
                        rs.getLong("adresa_id"),
                        rs.getString("kontakt_telefon"),
                        rs.getString("biljeska")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju svjedoka", e);
        }
        return svjedoci;
    }
}