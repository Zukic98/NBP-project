package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import org.springframework.stereotype.Repository;
import ba.unsa.etf.suds.dto.UposlenikLoginDTO;
import ba.unsa.etf.suds.model.UposlenikProfil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;


/**
 * Repozitorij za upravljanje uposlenicima i njihovim profilima iz tabela {@code nbp.NBP_USER},
 * {@code UPOSLENIK_PROFIL} i {@code CRNA_LISTA_TOKENA}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class UposlenikRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public UposlenikRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Dohvata podatke za prijavu uposlenika prema e-mail adresi i broju značke.
     *
     * @param email  e-mail adresa uposlenika
     * @param znacka broj značke uposlenika
     * @return {@link Optional} koji sadrži {@link ba.unsa.etf.suds.dto.UposlenikLoginDTO}, ili prazan ako nije pronađen
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public Optional<UposlenikLoginDTO> findByEmailAndZnacka(String email, String znacka) {
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.PASSWORD,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, p.STATUS,
                p.STANICA_ID
            FROM nbp.NBP_USER u
            JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            WHERE u.EMAIL = ? AND p.BROJ_ZNACKE = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, znacka);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UposlenikLoginDTO(
                        rs.getLong("USER_ID"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getString("EMAIL"),
                        rs.getString("PASSWORD"), 
                        rs.getString("ULOGA"),
                        rs.getString("BROJ_ZNACKE"),
                        rs.getString("STATUS"),    
                        rs.getLong("STANICA_ID")  
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri provjeri logina", e);
        }
        return Optional.empty();
    }

    /**
     * Dohvata sve uposlenike iz svih stanica kao DTO.
     *
     * @return lista svih {@link ba.unsa.etf.suds.dto.UposlenikDTO} objekata
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<UposlenikDTO> findAllUposlenici() {
        List<UposlenikDTO> uposlenici = new ArrayList<>();
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
                p.STATUS,  
                s.IME_STANICE
            FROM nbp.NBP_USER u
            JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            JOIN STANICE s ON p.STANICA_ID = s.STANICA_ID
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                uposlenici.add(mapRowToDTO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju svih uposlenika", e);
        }
        return uposlenici;
    }
    /**
     * Dohvata sve uposlenike određene stanice kao DTO.
     *
     * @param stanicaId identifikator stanice
     * @return lista {@link ba.unsa.etf.suds.dto.UposlenikDTO} objekata za datu stanicu
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<UposlenikDTO> findAllByStanicaId(Long stanicaId) {
        List<UposlenikDTO> uposlenici = new ArrayList<>();
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
                p.STATUS,
                s.IME_STANICE
            FROM nbp.NBP_USER u
            JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            JOIN STANICE s ON p.STANICA_ID = s.STANICA_ID
            WHERE p.STANICA_ID = ?
        """;

        try (Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, stanicaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    uposlenici.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju uposlenika stanice", e);
        }
        return uposlenici;
    }
    /**
     * Dohvata uposlenika prema identifikatoru korisnika kao DTO.
     *
     * @param userId identifikator korisnika
     * @return {@link Optional} koji sadrži {@link ba.unsa.etf.suds.dto.UposlenikDTO}, ili prazan ako nije pronađen
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public Optional<UposlenikDTO> findByUserId(Long userId) {
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
                p.STATUS,  
                s.IME_STANICE
            FROM nbp.NBP_USER u
            JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID
            JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            JOIN STANICE s ON p.STANICA_ID = s.STANICA_ID
            WHERE u.ID = ?
        """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
    System.out.println("SQL DETALJI: " + e.getMessage()); 
    e.printStackTrace();
    throw new RuntimeException("Greška pri dohvatanju uposlenika po ID-u: " + e.getMessage(), e);
}
        return Optional.empty();
    }

   /**
    * Dodaje JWT token u tabelu {@code CRNA_LISTA_TOKENA} radi invalidacije pri odjavi.
    *
    * @param token JWT token koji se stavlja na crnu listu
    * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
    */
   public void dodajUTabeluCrnaLista(String token) {
    String sql = "INSERT INTO CRNA_LISTA_TOKENA (TOKEN, EXPIRES_AT) VALUES (?, CURRENT_TIMESTAMP + 1)"; 
    try (Connection conn = dbManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, token);
        stmt.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri spremanju tokena u crnu listu", e);
    }
}

/**
 * Provjerava da li se JWT token nalazi u tabeli {@code CRNA_LISTA_TOKENA}.
 *
 * @param token JWT token koji se provjerava
 * @return {@code true} ako je token na crnoj listi, inače {@code false}
 */
public boolean jeLiTokenUcrnojListi(String token) {
    String sql = "SELECT COUNT(*) FROM CRNA_LISTA_TOKENA WHERE TOKEN = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, token);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    } catch (SQLException e) {
        return false;
    }
    return false;
}

    private UposlenikDTO mapRowToDTO(ResultSet rs) throws SQLException {
        return new UposlenikDTO(
                rs.getLong("USER_ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("USERNAME"),
                rs.getString("ULOGA"),
                rs.getString("BROJ_ZNACKE"),
                rs.getString("IME_STANICE"),
                rs.getString("STATUS") 
        );
    }

    // ========== HR MODUL  ==========

/**
 * Atomično kreira novog uposlenika u tabelama {@code nbp.NBP_USER} i {@code UPOSLENIK_PROFIL}
 * unutar jedne transakcije. U slučaju greške transakcija se poništava (rollback).
 *
 * @param request        zahtjev s podacima o novom uposleniku
 * @param stanicaId      identifikator stanice kojoj uposlenik pripada
 * @param encodedPassword hashirana lozinka
 * @return generirani {@code USER_ID} novog uposlenika
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public int insertUposlenik(DodajUposlenikaRequest request, Long stanicaId, String encodedPassword) {
    String sqlUser = "INSERT INTO nbp.NBP_USER (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, USERNAME, PHONE_NUMBER, BIRTH_DATE, ADDRESS_ID, ROLE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    String sqlProfil = "INSERT INTO UPOSLENIK_PROFIL (USER_ID, STANICA_ID, BROJ_ZNACKE, STATUS) VALUES (?, ?, ?, 'Aktivan')";
    
    Connection conn = null;
    PreparedStatement psUser = null;
    PreparedStatement psProfil = null;
    ResultSet generatedKeys = null;
    
    try {
        conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        psUser = conn.prepareStatement(sqlUser, new String[]{"ID"});
        psUser.setString(1, request.getFirstName());
        psUser.setString(2, request.getLastName());
        psUser.setString(3, request.getEmail());
        psUser.setString(4, encodedPassword);
        psUser.setString(5, request.getUsername());
        psUser.setString(6, request.getPhoneNumber());
        psUser.setDate(7, request.getBirthDate() != null ? java.sql.Date.valueOf(request.getBirthDate()) : null);
        psUser.setObject(8, request.getAddressId());
        psUser.setLong(9, request.getRoleId());
        psUser.executeUpdate();
        
        generatedKeys = psUser.getGeneratedKeys();
        Long userId = null;
        if (generatedKeys.next()) {
            userId = generatedKeys.getLong(1);
        } else {
            throw new SQLException("Kreiranje korisnika nije uspjelo");
        }
        
        psProfil = conn.prepareStatement(sqlProfil);
        psProfil.setLong(1, userId);
        psProfil.setLong(2, stanicaId);
        psProfil.setString(3, request.getBrojZnacke());
        psProfil.executeUpdate();
        
        conn.commit();
        return userId.intValue();
        
    } catch (SQLException e) {
        if (conn != null) { try { conn.rollback(); } catch (SQLException ex) {} }
        throw new RuntimeException("Greška pri dodavanju uposlenika: " + e.getMessage(), e);
    } finally {
        if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) {}
        if (psUser != null) try { psUser.close(); } catch (SQLException e) {}
        if (psProfil != null) try { psProfil.close(); } catch (SQLException e) {}
        if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {} }
    }
}

/**
 * Ažurira status uposlenika u tabeli {@code UPOSLENIK_PROFIL}.
 *
 * @param userId    identifikator korisnika
 * @param newStatus novi status (npr. {@code "Aktivan"} ili {@code "Neaktivan"})
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public void updateStatus(Long userId, String newStatus) {
    String sql = "UPDATE UPOSLENIK_PROFIL SET STATUS = ? WHERE USER_ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, newStatus);
        ps.setLong(2, userId);
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri ažuriranju statusa: " + e.getMessage(), e);
    }
}

/**
 * Broji aktivne šefove stanice za određenu stanicu.
 *
 * @param stanicaId identifikator stanice
 * @return broj aktivnih šefova stanice
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public int countActiveSefovaPoStanici(Long stanicaId) {
    String sql = "SELECT COUNT(*) FROM nbp.NBP_USER u JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID WHERE p.STANICA_ID = ? AND p.STATUS = 'Aktivan' AND r.NAME = 'SEF_STANICE'";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, stanicaId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri brojanju šefova: " + e.getMessage(), e);
    }
}

/**
 * Provjerava da li postoji korisnik s datom e-mail adresom.
 *
 * @param email e-mail adresa koja se provjerava
 * @return {@code true} ako postoji, inače {@code false}
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public boolean existsByEmail(String email) {
    String sql = "SELECT COUNT(*) FROM nbp.NBP_USER WHERE EMAIL = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri provjeri emaila: " + e.getMessage(), e);
    }
}

/**
 * Provjerava da li postoji korisnik s datim korisničkim imenom.
 *
 * @param username korisničko ime koje se provjerava
 * @return {@code true} ako postoji, inače {@code false}
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public boolean existsByUsername(String username) {
    String sql = "SELECT COUNT(*) FROM nbp.NBP_USER WHERE USERNAME = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri provjeri usernamea: " + e.getMessage(), e);
    }
}

/**
 * Provjerava da li postoji uposlenik s datim brojem značke.
 *
 * @param brojZnacke broj značke koji se provjerava
 * @return {@code true} ako postoji, inače {@code false}
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public boolean existsByBrojZnacke(String brojZnacke) {
    String sql = "SELECT COUNT(*) FROM UPOSLENIK_PROFIL WHERE BROJ_ZNACKE = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, brojZnacke);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri provjeri broja značke: " + e.getMessage(), e);
    }
}

/**
 * Provjerava da li korisnik pripada određenoj stanici.
 *
 * @param userId    identifikator korisnika
 * @param stanicaId identifikator stanice
 * @return {@code true} ako korisnik pripada stanici, inače {@code false}
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public boolean isUserInStanica(Long userId, Long stanicaId) {
    String sql = "SELECT COUNT(*) FROM UPOSLENIK_PROFIL WHERE USER_ID = ? AND STANICA_ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, userId);
        ps.setLong(2, stanicaId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri provjeri stanice: " + e.getMessage(), e);
    }
}

/**
 * Dohvata naziv uloge korisnika prema identifikatoru.
 *
 * @param userId identifikator korisnika
 * @return naziv uloge, ili {@code null} ako korisnik nije pronađen
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public String getUserRoleName(Long userId) {
    String sql = "SELECT r.NAME FROM nbp.NBP_USER u JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID WHERE u.ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("NAME");
            return null;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri dohvatanju uloge: " + e.getMessage(), e);
    }
}

/**
 * Dohvata profil uposlenika iz tabele {@code UPOSLENIK_PROFIL} prema identifikatoru korisnika.
 *
 * @param userId identifikator korisnika
 * @return {@link Optional} koji sadrži {@link ba.unsa.etf.suds.model.UposlenikProfil}, ili prazan ako nije pronađen
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public Optional<UposlenikProfil> findProfilByUserId(Long userId) {
    String sql = "SELECT PROFIL_ID, USER_ID, STANICA_ID, BROJ_ZNACKE, STATUS FROM UPOSLENIK_PROFIL WHERE USER_ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new UposlenikProfil(
                    rs.getLong("PROFIL_ID"),
                    rs.getLong("USER_ID"),
                    rs.getLong("STANICA_ID"),
                    rs.getString("BROJ_ZNACKE"),
                    rs.getString("STATUS")
                ));
            }
            return Optional.empty();
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri dohvatanju profila: " + e.getMessage(), e);
    }
}

/**
 * Provjerava da li postoji drugi korisnik s datom e-mail adresom (isključujući korisnika s datim ID-om).
 * Koristi se pri ažuriranju profila radi provjere jedinstvenosti e-maila.
 *
 * @param email  e-mail adresa koja se provjerava
 * @param userId identifikator korisnika koji se isključuje iz provjere
 * @return {@code true} ako drugi korisnik s tim e-mailom postoji, inače {@code false}
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public boolean existsByEmailAndNotUserId(String email, Long userId) {
    String sql = "SELECT COUNT(*) FROM nbp.NBP_USER WHERE EMAIL = ? AND ID != ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        ps.setLong(2, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri provjeri emaila: " + e.getMessage(), e);
    }
}

/**
 * Ažurira osnove podatke korisnika (ime, prezime, e-mail) u tabeli {@code nbp.NBP_USER}.
 *
 * @param userId  identifikator korisnika
 * @param ime     novo ime
 * @param prezime novo prezime
 * @param email   nova e-mail adresa
 * @throws RuntimeException ako korisnik nije pronađen ili dođe do greške pri izvršavanju SQL upita
 */
public void updateBasicInfo(Long userId, String ime, String prezime, String email) {
    String sql = "UPDATE nbp.NBP_USER SET FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ? WHERE ID = ?";
    
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, ime);
        ps.setString(2, prezime);
        ps.setString(3, email);
        ps.setLong(4, userId);
        
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected == 0) {
            throw new RuntimeException("Ažuriranje nije uspjelo, uposlenik sa ID " + userId + " nije pronađen.");
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri ažuriranju podataka u bazi: " + e.getMessage(), e);
    }

    
}

/**
 * Ažurira hashiranu lozinku korisnika u tabeli {@code nbp.NBP_USER}.
 *
 * @param userId          identifikator korisnika
 * @param encodedPassword nova hashirana lozinka
 * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
 */
public void updatePassword(Long userId, String encodedPassword) {
    String sql = "UPDATE nbp.NBP_USER SET PASSWORD = ? WHERE ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, encodedPassword);
        ps.setLong(2, userId);
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri ažuriranju lozinke: " + e.getMessage(), e);
    }
}

/**
 * Dohvata hashiranu lozinku korisnika prema identifikatoru.
 *
 * @param userId identifikator korisnika
 * @return hashirana lozinka
 * @throws RuntimeException ako korisnik nije pronađen ili dođe do greške pri izvršavanju SQL upita
 */
public String getPasswordByUserId(Long userId) {
    String sql = "SELECT PASSWORD FROM nbp.NBP_USER WHERE ID = ?";
    try (Connection conn = dbManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("PASSWORD");
            throw new RuntimeException("Korisnik nije pronađen");
        }
    } catch (SQLException e) {
        throw new RuntimeException("Greška pri dohvaćanju lozinke");
    }
}

}