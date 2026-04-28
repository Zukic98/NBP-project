package ba.unsa.etf.suds.repository;

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


public KrivicnoDjelo save(KrivicnoDjelo djelo) {
    String sql = "INSERT INTO KRIVICNA_DJELA (NAZIV, KATEGORIJA, KAZNENI_ZAKON_CLAN) VALUES (?, ?, ?)";
    
    try (Connection conn = dbManager.getConnection()) {
        // Isključi auto-commit za transakciju
        conn.setAutoCommit(false);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"DJELO_ID"})) {
            stmt.setString(1, djelo.getNaziv());
            stmt.setString(2, djelo.getKategorija());
            stmt.setString(3, djelo.getKazneniZakonClan());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                throw new SQLException("Kreiranje krivičnog djela nije uspjelo.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    djelo.setId(generatedKeys.getLong(1));
                } else {
                    conn.rollback();
                    throw new SQLException("Nije dobijen ID kreiranog djela.");
                }
            }
            
            conn.commit();
            return djelo;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri kreiranju krivičnog djela: " + e.getMessage(), e);
    }
}

    public KrivicnoDjelo update(KrivicnoDjelo djelo) {
        String sql = "UPDATE KRIVICNA_DJELA SET NAZIV = ?, KATEGORIJA = ?, KAZNENI_ZAKON_CLAN = ? WHERE DJELO_ID = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, djelo.getNaziv());
            stmt.setString(2, djelo.getKategorija());
            stmt.setString(3, djelo.getKazneniZakonClan());
            stmt.setLong(4, djelo.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Ažuriranje nije uspjelo. Krivično djelo nije pronađeno.");
            }
            
            return djelo;
            
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju krivičnog djela: " + e.getMessage(), e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM KRIVICNA_DJELA WHERE DJELO_ID = ?";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Brisanje nije uspjelo. Krivično djelo nije pronađeno.");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri brisanju krivičnog djela: " + e.getMessage(), e);
        }
}

/**
 * Provjerava da li krivično djelo sa istim nazivom i članom već postoji
 */
    public boolean postojiDuplikat(String naziv, String kazneniZakonClan) {
        String sql = "SELECT COUNT(*) FROM KRIVICNA_DJELA WHERE UPPER(NAZIV) = UPPER(?) AND UPPER(KAZNENI_ZAKON_CLAN) = UPPER(?)";
        
        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, naziv.trim());
            stmt.setString(2, kazneniZakonClan.trim());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri provjeri duplikata krivičnog djela: " + e.getMessage(), e);
        }
        return false;
    }

}