package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Adresa;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AdresaRepository {

    private final DatabaseManager dbManager;

    public AdresaRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // 1. Get all addresses (SELECT)
    public List<Adresa> findAll() {
        List<Adresa> adrese = new ArrayList<>();
        String sql = "SELECT * FROM Adrese";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                adrese.add(mapRowToAdresa(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju adresa iz baze", e);
        }
        return adrese;
    }

    // 2. Put new address
    public Adresa save(Adresa adresa) {
        String sql = "INSERT INTO Adrese (ulica_i_broj, grad, postanski_broj, drzava) VALUES (?, ?, ?, ?)";

        // Using RETURN_GENERATED_KEYS so the Oracle get ID  which is at this moment come
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, adresa.getUlicaIBroj());
            stmt.setString(2, adresa.getGrad());
            stmt.setString(3, adresa.getPostanskiBroj());
            stmt.setString(4, adresa.getDrzava());

            stmt.executeUpdate();

            // Taking generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adresa.setAdresaId(generatedKeys.getLong(1));
                }
            }
            return adresa;
        } catch (SQLException e) {
            throw new RuntimeException("Greska prilikom spasavanja adrese", e);
        }
    }

    public Long saveWithConnection(Connection conn, Adresa adresa) throws SQLException {
        String sql = "INSERT INTO Adrese (ulica_i_broj, grad, postanski_broj, drzava) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, adresa.getUlicaIBroj());
            stmt.setString(2, adresa.getGrad());
            stmt.setString(3, adresa.getPostanskiBroj());
            stmt.setString(4, adresa.getDrzava());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new SQLException("No generated key returned for Adresa insert");
        }
    }

    private Adresa mapRowToAdresa(ResultSet rs) throws SQLException {
        return new Adresa(
                rs.getLong("adresa_id"),
                rs.getString("ulica_i_broj"),
                rs.getString("grad"),
                rs.getString("postanski_broj"),
                rs.getString("drzava")
        );
    }
}