package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.MojSlucajDTO;
import ba.unsa.etf.suds.dto.SlucajDetaljiDTO;
import ba.unsa.etf.suds.dto.SlucajListDTO;
import ba.unsa.etf.suds.model.Slucaj;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij za upravljanje slučajevima iz tabele {@code SLUCAJEVI}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class SlucajRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public SlucajRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Sprema novi slučaj u tabelu {@code SLUCAJEVI}.
     *
     * @param slucaj slučaj koji se sprema
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata sve slučajeve iz tabele {@code SLUCAJEVI}.
     *
     * @return lista svih slučajeva
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata slučaj prema identifikatoru.
     *
     * @param id identifikator slučaja
     * @return {@link Optional} koji sadrži slučaj, ili prazan ako nije pronađen
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Ažurira opis, status i voditelja slučaja u tabeli {@code SLUCAJEVI}.
     *
     * @param slucaj slučaj s novim podacima
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Briše slučaj iz tabele {@code SLUCAJEVI} prema identifikatoru.
     *
     * @param id identifikator slučaja koji se briše
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Dohvata detalje slučaja kao DTO putem JOIN-a s tabelama osumnjičenih i krivičnih djela.
     *
     * @param brojSlucaja broj slučaja koji se traži
     * @return popunjeni {@link ba.unsa.etf.suds.dto.SlucajDetaljiDTO}
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
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

    /**
     * Sprema novi slučaj koristeći proslijeđenu konekciju i vraća generirani primarni ključ.
     * Namijenjen za upotrebu unutar transakcija kojima upravlja pozivatelj.
     *
     * @param conn   aktivna SQL konekcija kojom upravlja pozivatelj
     * @param slucaj slučaj koji se sprema
     * @return generirani {@code SLUCAJ_ID}
     * @throws SQLException ako dođe do greške pri izvršavanju SQL upita
     */
    public Long saveWithConnection(Connection conn, Slucaj slucaj) throws SQLException {
        String sql = "INSERT INTO SLUCAJEVI (STANICA_ID, BROJ_SLUCAJA, OPIS, STATUS, VODITELJ_USER_ID, DATUM_KREIRANJA) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"SLUCAJ_ID"})) {
            stmt.setLong(1, slucaj.getStanicaId());
            stmt.setString(2, slucaj.getBrojSlucaja());
            stmt.setString(3, slucaj.getOpis());
            stmt.setString(4, slucaj.getStatus() != null ? slucaj.getStatus() : "Otvoren");
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

    /**
     * Dohvata filtrirani spisak slučajeva kao DTO ovisno o ulozi korisnika.
     * Šef stanice vidi sve slučajeve svoje stanice; ostali vide samo slučajeve
     * na kojima su voditelji ili članovi tima.
     *
     * @param userId   identifikator korisnika
     * @param roleName naziv uloge korisnika
     * @return lista {@link ba.unsa.etf.suds.dto.SlucajListDTO} objekata
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<SlucajListDTO> findAllFiltered(Long userId, String roleName) {
        List<SlucajListDTO> rezultat = new ArrayList<>();
        String sql;

        if ("ŠEF".equalsIgnoreCase(roleName) || "SEF_STANICE".equalsIgnoreCase(roleName)) {
            sql = "SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, " +
                    "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS VODITELJ, s.DATUM_KREIRANJA " +
                    "FROM SLUCAJEVI s " +
                    "JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                    "WHERE s.STANICA_ID = (SELECT STANICA_ID FROM UPOSLENIK_PROFIL WHERE USER_ID = ?)";
        } else {
            sql = "SELECT DISTINCT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, " +
                    "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS VODITELJ, s.DATUM_KREIRANJA " +
                    "FROM SLUCAJEVI s " +
                    "JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                    "LEFT JOIN TIM_NA_SLUCAJU t ON s.SLUCAJ_ID = t.SLUCAJ_ID " +
                    "WHERE s.VODITELJ_USER_ID = ? OR t.USER_ID = ?";
        }

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            if (!("ŠEF".equalsIgnoreCase(roleName) || "SEF_STANICE".equalsIgnoreCase(roleName))) {
                stmt.setLong(2, userId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rezultat.add(mapRowToSlucajListDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching filtered case list for userId: " + userId, e);
        }

        return rezultat;
    }

    /**
     * Dohvata slučaj s imenom voditelja kao DTO prema identifikatoru.
     *
     * @param id identifikator slučaja
     * @return {@link Optional} koji sadrži {@link ba.unsa.etf.suds.dto.SlucajListDTO}, ili prazan ako nije pronađen
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public Optional<SlucajListDTO> findByIdWithVoditelj(Long id) {
        String sql = "SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, s.DATUM_KREIRANJA, " +
                "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS VODITELJ " +
                "FROM SLUCAJEVI s " +
                "JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                "WHERE s.SLUCAJ_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSlucajListDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case list DTO by ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Ažurira status slučaja u tabeli {@code SLUCAJEVI}.
     *
     * @param id     identifikator slučaja
     * @param status novi status
     * @return {@code true} ako je ažuriran barem jedan red, inače {@code false}
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE SLUCAJEVI SET STATUS = ? WHERE SLUCAJ_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating case status for ID: " + id, e);
        }
    }

    /**
     * Dohvata slučajeve korisnika s ulogom na slučaju kao DTO.
     * Inspektori i šefovi vide slučajeve na kojima su voditelji ili članovi tima;
     * ostale uloge vide samo slučajeve na kojima su članovi tima.
     *
     * @param userId   identifikator korisnika
     * @param roleName naziv uloge korisnika
     * @return lista {@link ba.unsa.etf.suds.dto.MojSlucajDTO} objekata
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<MojSlucajDTO> findMojiSlucajevi(Long userId, String roleName) {
        List<MojSlucajDTO> rezultat = new ArrayList<>();
        String sql;
        boolean voditeljniRole = "Inspektor".equalsIgnoreCase(roleName)
                || "INSPEKTOR".equalsIgnoreCase(roleName)
                || "Šef".equalsIgnoreCase(roleName)
                || "SEF_STANICE".equalsIgnoreCase(roleName);

        if (voditeljniRole) {
            sql = "SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, s.DATUM_KREIRANJA, " +
                  "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS IME_VODITELJA, " +
                  "CASE WHEN s.VODITELJ_USER_ID = ? THEN 'Voditelj' ELSE t.ULOGA_NA_SLUCAJU END AS ULOGA " +
                  "FROM SLUCAJEVI s " +
                  "LEFT JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                  "LEFT JOIN TIM_NA_SLUCAJU t ON s.SLUCAJ_ID = t.SLUCAJ_ID AND t.USER_ID = ? " +
                  "WHERE s.VODITELJ_USER_ID = ? OR t.USER_ID = ?";
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
            if (voditeljniRole) {
                stmt.setLong(1, userId);
                stmt.setLong(2, userId);
                stmt.setLong(3, userId);
                stmt.setLong(4, userId);
            } else {
                stmt.setLong(1, userId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                java.util.Set<Long> seen = new java.util.HashSet<>();
                while (rs.next()) {
                    Long slucajId = rs.getLong("SLUCAJ_ID");
                    if (!seen.add(slucajId)) {
                        continue;
                    }
                    MojSlucajDTO dto = new MojSlucajDTO();
                    dto.setSlucajId(slucajId);
                    dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
                    dto.setOpis(rs.getString("OPIS"));
                    dto.setStatus(rs.getString("STATUS"));
                    dto.setVoditeljSlucaja(rs.getString("IME_VODITELJA"));
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

    private SlucajListDTO mapRowToSlucajListDTO(ResultSet rs) throws SQLException {
        SlucajListDTO dto = new SlucajListDTO();
        dto.setSlucajId(rs.getLong("SLUCAJ_ID"));
        dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
        dto.setOpis(rs.getString("OPIS"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setVoditeljSlucaja(rs.getString("VODITELJ"));
        dto.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
        return dto;
    }
}
