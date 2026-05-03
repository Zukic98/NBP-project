package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.OsumnjiceniDTO;
import ba.unsa.etf.suds.model.Osumnjiceni;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij za upravljanje osumnjičenima iz tabele {@code OSUMNJICENI} i vezne tabele {@code SLUCAJ_OSUMNJICENI}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class OsumnjiceniRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public OsumnjiceniRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Sprema novog osumnjičenog koristeći proslijeđenu konekciju (za transakcijsku upotrebu).
     * Vraća generisani {@code OSUMNJICENI_ID}.
     *
     * @param conn        aktivna JDBC konekcija
     * @param osumnjiceni osumnjičeni koji se sprema
     * @return generisani primarni ključ novog osumnjičenog
     * @throws SQLException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dodaje vezu između slučaja i osumnjičenog u tabelu {@code SLUCAJ_OSUMNJICENI} koristeći proslijeđenu konekciju.
     *
     * @param conn          aktivna JDBC konekcija
     * @param slucajId      identifikator slučaja
     * @param osumnjiceniId identifikator osumnjičenog
     * @throws SQLException ako dođe do greške pri izvršavanju SQL upita
     */
    public void linkToSlucaj(Connection conn, Long slucajId, Long osumnjiceniId) throws SQLException {
        String sql = "INSERT INTO SLUCAJ_OSUMNJICENI (SLUCAJ_ID, OSUMNJICENI_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            stmt.setLong(2, osumnjiceniId);
            stmt.executeUpdate();
        }
    }

    /**
     * Sprema novog osumnjičenog u tabelu {@code OSUMNJICENI}.
     *
     * @param osumnjiceni osumnjičeni koji se sprema
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata sve osumnjičene iz tabele {@code OSUMNJICENI}.
     *
     * @return lista svih osumnjičenih
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata osumnjičenog po primarnom ključu.
     *
     * @param id identifikator osumnjičenog ({@code OSUMNJICENI_ID})
     * @return {@link Optional} sa pronađenim osumnjičenim, ili prazan ako ne postoji
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Ažurira podatke osumnjičenog u tabeli {@code OSUMNJICENI}.
     *
     * @param osumnjiceni osumnjičeni sa ažuriranim podacima (mora imati postavljen {@code osumnjiceniId})
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Briše osumnjičenog iz tabele {@code OSUMNJICENI} po ID-u.
     *
     * @param id identifikator osumnjičenog koji se briše
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata sve osumnjičene vezane za dati slučaj, sa JOIN-om na adrese.
     *
     * @param slucajId identifikator slučaja
     * @return lista DTO-ova sa podacima o osumnjičenima za dati slučaj
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<OsumnjiceniDTO> findBySlucajId(Long slucajId) {
    List<OsumnjiceniDTO> lista = new ArrayList<>();
    String sql = "SELECT o.OSUMNJICENI_ID, o.IME_PREZIME, o.JMBG, o.DATUM_RODJENJA, " +
                 "(a.ULICA_I_BROJ || ', ' || a.GRAD) AS ADRESA " +
                 "FROM OSUMNJICENI o " +
                 "JOIN SLUCAJ_OSUMNJICENI so ON o.OSUMNJICENI_ID = so.OSUMNJICENI_ID " +
                 "LEFT JOIN ADRESE a ON o.ADRESA_ID = a.ADRESA_ID " +
                 "WHERE so.SLUCAJ_ID = ?";

    try (Connection conn = dbManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, slucajId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new OsumnjiceniDTO(
                        rs.getLong("OSUMNJICENI_ID"),
                        rs.getString("IME_PREZIME"),
                        rs.getString("JMBG"),
                        rs.getString("ADRESA"),
                        rs.getDate("DATUM_RODJENJA")
                ));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Error while fetching suspects for case: " + slucajId, e);
    }
    return lista;
}
}