package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.TimClanDTO;
import ba.unsa.etf.suds.model.TimNaSlucaju;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za upravljanje članovima tima na slučaju iz tabele {@code TIM_NA_SLUCAJU}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class TimNaSlucajuRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public TimNaSlucajuRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Sprema dodjelu člana tima koristeći proslijeđenu konekciju.
     * Namijenjen za upotrebu unutar transakcija kojima upravlja pozivatelj.
     *
     * @param conn aktivna SQL konekcija kojom upravlja pozivatelj
     * @param tim  dodjela člana tima koja se sprema
     * @throws SQLException ako dođe do greške pri izvršavanju SQL upita
     */
    public void saveWithConnection(Connection conn, TimNaSlucaju tim) throws SQLException {
        String sql = "INSERT INTO TIM_NA_SLUCAJU (SLUCAJ_ID, USER_ID, ULOGA_NA_SLUCAJU) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tim.getSlucajId());
            stmt.setLong(2, tim.getUserId());
            stmt.setString(3, tim.getUlogaNaSlucaju());
            stmt.executeUpdate();
        }
    }

    /**
     * Dohvata sve članove tima za određeni slučaj kao DTO s podacima o korisniku i ulozi.
     *
     * @param caseId identifikator slučaja
     * @return lista {@link ba.unsa.etf.suds.dto.TimClanDTO} objekata
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<TimClanDTO> findByCaseId(Long caseId) {
        List<TimClanDTO> clanovi = new ArrayList<>();
        String sql = "SELECT t.DODJELA_ID, t.USER_ID, (u.FIRST_NAME||' '||u.LAST_NAME) AS IME_PREZIME, " +
                "r.NAME AS NAZIV_ULOGE, t.ULOGA_NA_SLUCAJU, p.BROJ_ZNACKE, u.EMAIL " +
                "FROM TIM_NA_SLUCAJU t " +
                "JOIN nbp.NBP_USER u ON t.USER_ID=u.ID " +
                "JOIN nbp.NBP_ROLE r ON u.ROLE_ID=r.ID " +
                "LEFT JOIN UPOSLENIK_PROFIL p ON u.ID=p.USER_ID " +
                "WHERE t.SLUCAJ_ID=? " +
                "ORDER BY t.DODJELA_ID";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, caseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TimClanDTO dto = new TimClanDTO();
                    dto.setDodjelaId(rs.getLong("DODJELA_ID"));
                    dto.setUposlenikId(rs.getLong("USER_ID"));
                    dto.setImePrezime(rs.getString("IME_PREZIME"));
                    dto.setNazivUloge(rs.getString("NAZIV_ULOGE"));
                    dto.setUlogaNaSlucaju(rs.getString("ULOGA_NA_SLUCAJU"));
                    dto.setBrojZnacke(rs.getString("BROJ_ZNACKE"));
                    dto.setEmail(rs.getString("EMAIL"));
                    clanovi.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case team members", e);
        }

        return clanovi;
    }

    /**
     * Sprema dodjelu člana tima u tabelu {@code TIM_NA_SLUCAJU} i vraća generirani primarni ključ.
     *
     * @param tim dodjela člana tima koja se sprema
     * @return generirani {@code DODJELA_ID}
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public Long save(TimNaSlucaju tim) {
        String sql = "INSERT INTO TIM_NA_SLUCAJU (SLUCAJ_ID, USER_ID, ULOGA_NA_SLUCAJU) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"DODJELA_ID"})) {
            stmt.setLong(1, tim.getSlucajId());
            stmt.setLong(2, tim.getUserId());
            stmt.setString(3, tim.getUlogaNaSlucaju());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    tim.setId(generatedId);
                    return generatedId;
                }
            }

            throw new RuntimeException("No generated key returned for TIM_NA_SLUCAJU insert");
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving team assignment", e);
        }
    }

    /**
     * Briše dodjelu člana tima iz tabele {@code TIM_NA_SLUCAJU} prema identifikatoru dodjele.
     *
     * @param id identifikator dodjele koja se briše
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM TIM_NA_SLUCAJU WHERE DODJELA_ID=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting team assignment by ID: " + id, e);
        }
    }
}
