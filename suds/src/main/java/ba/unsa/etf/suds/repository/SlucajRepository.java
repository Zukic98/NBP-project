package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.MojSlucajDTO;
import ba.unsa.etf.suds.dto.SlucajDetaljiDTO;
import ba.unsa.etf.suds.model.Slucaj;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SlucajRepository {
    private final DatabaseManager dbManager;

    public SlucajRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // CREATE
    public void save(Slucaj slucaj) {
        String sql = "INSERT INTO SLUCAJEVI (STANICA_ID, BROJ_SLUCAJA, OPIS, STATUS, VODITELJ_USER_ID, DATUM_KREIRANJA) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucaj.getStanicaId());
            stmt.setString(2, slucaj.getBrojSlucaja());
            stmt.setString(3, slucaj.getOpis());
            stmt.setString(4, slucaj.getStatus());
            stmt.setLong(5, slucaj.getVoditeljUserId());
            stmt.setTimestamp(6, slucaj.getDatumKreiranja());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving case to database", e);
        }
    }

    // READ ALL
    public List<Slucaj> findAll() {
        List<Slucaj> slucajevi = new ArrayList<>();
        String sql = "SELECT * FROM SLUCAJEVI";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                slucajevi.add(mapRowToSlucaj(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching all cases", e);
        }
        return slucajevi;
    }

    // READ BY ID
    public Optional<Slucaj> findById(Long id) {
        String sql = "SELECT * FROM SLUCAJEVI WHERE SLUCAJ_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSlucaj(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case by ID: " + id, e);
        }
        return Optional.empty();
    }

    // UPDATE
    public void update(Slucaj slucaj) {
        String sql = "UPDATE SLUCAJEVI SET OPIS = ?, STATUS = ?, VODITELJ_USER_ID = ? WHERE SLUCAJ_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, slucaj.getOpis());
            stmt.setString(2, slucaj.getStatus());
            stmt.setLong(3, slucaj.getVoditeljUserId());
            stmt.setLong(4, slucaj.getSlucajId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating case with ID: " + slucaj.getSlucajId(), e);
        }
    }

    // DELETE
    public void delete(Long id) {
        String sql = "DELETE FROM SLUCAJEVI WHERE SLUCAJ_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting case with ID: " + id, e);
        }
    }

    // COMPLEX READ FOR DTO
    public SlucajDetaljiDTO findDetaljiByBroj(String brojSlucaja) {
        SlucajDetaljiDTO dto = new SlucajDetaljiDTO();
        dto.setOsumnjiceni(new ArrayList<>());
        dto.setKrivicnaDjela(new ArrayList<>());

        
        String sql = "SELECT s.BROJ_SLUCAJA, s.OPIS, (u.FIRST_NAME || ' ' || u.LAST_NAME) as INSPEKTOR, " +
             "o.IME_PREZIME, kd.NAZIV " +
             "FROM SLUCAJEVI s " + 
             "LEFT JOIN nbp.nbp_user u ON s.VODITELJ_USER_ID = u.ID " + 
             "LEFT JOIN SLUCAJ_OSUMNJICENI so ON s.SLUCAJ_ID = so.SLUCAJ_ID " +
             "LEFT JOIN OSUMNJICENI o ON so.OSUMNJICENI_ID = o.OSUMNJICENI_ID " +
             "LEFT JOIN SLUCAJ_KRIVICNO_DJELO skd ON s.SLUCAJ_ID = skd.SLUCAJ_ID " +
             "LEFT JOIN KRIVICNA_DJELA kd ON skd.DJELO_ID = kd.DJELO_ID " +
             "WHERE s.BROJ_SLUCAJA = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, brojSlucaja);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (dto.getBrojSlucaja() == null) {
                        dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
                        dto.setOpis(rs.getString("OPIS"));
                        dto.setImeInspektora(rs.getString("INSPEKTOR"));
                    }
                    String oIme = rs.getString("IME_PREZIME");
                    if (oIme != null && !dto.getOsumnjiceni().contains(oIme)) dto.getOsumnjiceni().add(oIme);
                    String djelo = rs.getString("NAZIV");
                    if (djelo != null && !dto.getKrivicnaDjela().contains(djelo)) dto.getKrivicnaDjela().add(djelo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case details DTO for number: " + brojSlucaja, e);
        }
        return dto;
    }

    public Long saveWithConnection(Connection conn, Slucaj slucaj) throws SQLException {
        String sql = "INSERT INTO SLUCAJEVI (STANICA_ID, BROJ_SLUCAJA, OPIS, STATUS, VODITELJ_USER_ID, DATUM_KREIRANJA) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, slucaj.getStanicaId());
            stmt.setString(2, slucaj.getBrojSlucaja());
            stmt.setString(3, slucaj.getOpis());
            stmt.setString(4, slucaj.getStatus() != null ? slucaj.getStatus() : "Aktivan");
            stmt.setLong(5, slucaj.getVoditeljUserId());
            stmt.setTimestamp(6, slucaj.getDatumKreiranja() != null
                    ? slucaj.getDatumKreiranja()
                    : new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            throw new SQLException("No generated key returned for Slucaj insert");
        }
    }

    public List<MojSlucajDTO> findMojiSlucajevi(Long userId, String roleName) {
        List<MojSlucajDTO> rezultat = new ArrayList<>();
        String sql;

        if ("Inspektor".equalsIgnoreCase(roleName) || "Šef".equalsIgnoreCase(roleName)) {
            sql = "SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, s.DATUM_KREIRANJA, " +
                  "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS IME_VODITELJA, 'Voditelj' AS ULOGA " +
                  "FROM SLUCAJEVI s " +
                  "LEFT JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                  "WHERE s.VODITELJ_USER_ID = ?";
        } else {
            sql = "SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, s.DATUM_KREIRANJA, " +
                  "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS IME_VODITELJA, t.ULOGA_NA_SLUCAJU AS ULOGA " +
                  "FROM SLUCAJEVI s " +
                  "JOIN TIM_NA_SLUCAJU t ON s.SLUCAJ_ID = t.SLUCAJ_ID " +
                  "LEFT JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                  "WHERE t.USER_ID = ?";
        }

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MojSlucajDTO dto = new MojSlucajDTO();
                    dto.setSlucajId(rs.getLong("SLUCAJ_ID"));
                    dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
                    dto.setOpis(rs.getString("OPIS"));
                    dto.setStatus(rs.getString("STATUS"));
                    dto.setImeVoditelja(rs.getString("IME_VODITELJA"));
                    dto.setUlogaNaSlucaju(rs.getString("ULOGA"));
                    dto.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
                    rezultat.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching user cases for userId: " + userId, e);
        }
        return rezultat;
    }

    private Slucaj mapRowToSlucaj(ResultSet rs) throws SQLException {
        return new Slucaj(
                rs.getLong("SLUCAJ_ID"),
                rs.getLong("STANICA_ID"),
                rs.getString("BROJ_SLUCAJA"),
                rs.getString("OPIS"),
                rs.getString("STATUS"),
                rs.getLong("VODITELJ_USER_ID"),
                rs.getTimestamp("DATUM_KREIRANJA")
        );
    }
}