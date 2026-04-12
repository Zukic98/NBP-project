package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.Osumnjiceni;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OsumnjiceniRepository {
    private final DatabaseManager dbManager;

    public OsumnjiceniRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Long saveWithConnection(Connection conn, Osumnjiceni osumnjiceni) throws SQLException {
        String sql = "INSERT INTO OSUMNJICENI (IME_PREZIME, JMBG, ADRESA_ID, DATUM_RODJENJA) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"OSUMNJICENI_ID"})) {
            stmt.setString(1, osumnjiceni.getImePrezime());
            stmt.setString(2, osumnjiceni.getJmbg());
            stmt.setLong(3, osumnjiceni.getAdresaId());
            stmt.setDate(4, osumnjiceni.getDatumRodjenja());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new SQLException("No generated key returned for Osumnjiceni insert");
        }
    }

    public void linkToSlucaj(Connection conn, Long slucajId, Long osumnjiceniId) throws SQLException {
        String sql = "INSERT INTO SLUCAJ_OSUMNJICENI (SLUCAJ_ID, OSUMNJICENI_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            stmt.setLong(2, osumnjiceniId);
            stmt.executeUpdate();
        }
    }

    public void save(Osumnjiceni osumnjiceni) {
        String sql = "INSERT INTO OSUMNJICENI (IME_PREZIME, JMBG, ADRESA_ID, DATUM_RODJENJA) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, osumnjiceni.getImePrezime());
            stmt.setString(2, osumnjiceni.getJmbg());
            stmt.setLong(3, osumnjiceni.getAdresaId());
            stmt.setDate(4, osumnjiceni.getDatumRodjenja());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding new suspect", e);
        }
    }

    // READ ALL
    public List<Osumnjiceni> findAll() {
        List<Osumnjiceni> lista = new ArrayList<>();
        String sql = "SELECT * FROM OSUMNJICENI";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapRowToOsumnjiceni(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching all suspects", e);
        }
        return lista;
    }

    // READ BY ID
    public Optional<Osumnjiceni> findById(Long id) {
        String sql = "SELECT * FROM OSUMNJICENI WHERE OSUMNJICENI_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToOsumnjiceni(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching suspect with ID: " + id, e);
        }
        return Optional.empty();
    }

    // UPDATE
    public void update(Osumnjiceni osumnjiceni) {
        String sql = "UPDATE OSUMNJICENI SET IME_PREZIME = ?, JMBG = ?, ADRESA_ID = ? WHERE OSUMNJICENI_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, osumnjiceni.getImePrezime());
            stmt.setString(2, osumnjiceni.getJmbg());
            stmt.setLong(3, osumnjiceni.getAdresaId());
            stmt.setLong(4, osumnjiceni.getOsumnjiceniId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating suspect with ID: " + osumnjiceni.getOsumnjiceniId(), e);
        }
    }

    // DELETE
    public void delete(Long id) {
        String sql = "DELETE FROM OSUMNJICENI WHERE OSUMNJICENI_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting suspect with ID: " + id, e);
        }
    }

    private Osumnjiceni mapRowToOsumnjiceni(ResultSet rs) throws SQLException {
        return new Osumnjiceni(
                rs.getLong("OSUMNJICENI_ID"),
                rs.getString("IME_PREZIME"),
                rs.getString("JMBG"),
                rs.getLong("ADRESA_ID"),
                rs.getDate("DATUM_RODJENJA")
        );
    }
}