package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.SlucajKrivicnoDjelo;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SlucajKrivicnoDjeloRepository {
    private final DatabaseManager dbManager;

    public SlucajKrivicnoDjeloRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Dohvata sva krivična djela povezana sa slučajem (sa JOIN-om)
     */
    public List<SlucajKrivicnoDjelo> findBySlucajId(Long slucajId) {
        List<SlucajKrivicnoDjelo> veze = new ArrayList<>();
        String sql = """
            SELECT skd.VEZA_ID, skd.SLUCAJ_ID, skd.DJELO_ID,
                   kd.NAZIV as NAZIV_DJELA, kd.KATEGORIJA, kd.KAZNENI_ZAKON_CLAN
            FROM SLUCAJ_KRIVICNO_DJELO skd
            JOIN KRIVICNA_DJELA kd ON skd.DJELO_ID = kd.DJELO_ID
            WHERE skd.SLUCAJ_ID = ?
            ORDER BY kd.NAZIV
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, slucajId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SlucajKrivicnoDjelo veza = new SlucajKrivicnoDjelo();
                    veza.setVezaId(rs.getLong("VEZA_ID"));
                    veza.setSlucajId(rs.getLong("SLUCAJ_ID"));
                    veza.setDjeloId(rs.getLong("DJELO_ID"));
                    veza.setNazivDjela(rs.getString("NAZIV_DJELA"));
                    veza.setKategorija(rs.getString("KATEGORIJA"));
                    veza.setKazneniZakonClan(rs.getString("KAZNENI_ZAKON_CLAN"));
                    veze.add(veza);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju krivičnih djela za slučaj: " + e.getMessage(), e);
        }
        return veze;
    }

    /**
     * Dodaje vezu između slučaja i krivičnog djela
     */
    public SlucajKrivicnoDjelo dodajVezu(Long slucajId, Long djeloId) {
        String sql = "INSERT INTO SLUCAJ_KRIVICNO_DJELO (SLUCAJ_ID, DJELO_ID) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"VEZA_ID"})) {
                stmt.setLong(1, slucajId);
                stmt.setLong(2, djeloId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    throw new SQLException("Dodavanje veze nije uspjelo.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        SlucajKrivicnoDjelo veza = new SlucajKrivicnoDjelo();
                        veza.setVezaId(generatedKeys.getLong(1));
                        veza.setSlucajId(slucajId);
                        veza.setDjeloId(djeloId);

                        conn.commit();
                        return veza;
                    } else {
                        conn.rollback();
                        throw new SQLException("Nije dobijen ID kreirane veze.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dodavanju veze slučaj-djelo: " + e.getMessage(), e);
        }
    }

    /**
     * Dodaje više krivičnih djela na slučaj odjednom (bulk insert)
     */
    public void dodajViseVeza(Long slucajId, List<Long> djeloIds) {
        String sql = "INSERT INTO SLUCAJ_KRIVICNO_DJELO (SLUCAJ_ID, DJELO_ID) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Long djeloId : djeloIds) {
                    stmt.setLong(1, slucajId);
                    stmt.setLong(2, djeloId);
                    stmt.addBatch();
                }

                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dodavanju više krivičnih djela: " + e.getMessage(), e);
        }
    }

    /**
     * Briše vezu između slučaja i krivičnog djela
     */
    public void ukloniVezu(Long vezaId) {
        String sql = "DELETE FROM SLUCAJ_KRIVICNO_DJELO WHERE VEZA_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vezaId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Veza nije pronađena.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri brisanju veze slučaj-djelo: " + e.getMessage(), e);
        }
    }

    /**
     * Briše sve veze za dati slučaj
     */
    public void ukloniSveVezeZaSlucaj(Long slucajId) {
        String sql = "DELETE FROM SLUCAJ_KRIVICNO_DJELO WHERE SLUCAJ_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, slucajId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri brisanju svih veza za slučaj: " + e.getMessage(), e);
        }
    }

    /**
     * Provjerava da li veza već postoji
     */
    public boolean postojiVeza(Long slucajId, Long djeloId) {
        String sql = "SELECT COUNT(*) FROM SLUCAJ_KRIVICNO_DJELO WHERE SLUCAJ_ID = ? AND DJELO_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, slucajId);
            stmt.setLong(2, djeloId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri provjeri veze: " + e.getMessage(), e);
        }
        return false;
    }
}