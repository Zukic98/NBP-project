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


@Repository
public class UposlenikRepository {
    private final DatabaseManager dbManager;

    public UposlenikRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
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

    public List<UposlenikDTO> findAllUposlenici() {
        List<UposlenikDTO> uposlenici = new ArrayList<>();
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
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

    public Optional<UposlenikDTO> findByUserId(Long userId) {
        String sql = """
            SELECT 
                u.ID AS USER_ID, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.USERNAME,
                r.NAME AS ULOGA, 
                p.BROJ_ZNACKE, 
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
                rs.getString("IME_STANICE")
        );
    }

    // ========== HR MODUL  ==========

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

}