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

    public void saveWithConnection(Connection conn, Svjedok svjedok) throws SQLException {
        String sql = "INSERT INTO Svjedoci (SLUCAJ_ID, IME_PREZIME, JMBG, ADRESA_ID, KONTAKT_TELEFON, BILJESKA) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, svjedok.getSlucajId());
            stmt.setString(2, svjedok.getImePrezime());
            stmt.setString(3, svjedok.getJmbg());
            stmt.setLong(4, svjedok.getAdresaId());
            stmt.setString(5, svjedok.getKontaktTelefon());
            stmt.setString(6, svjedok.getBiljeska());
            stmt.executeUpdate();
        }
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