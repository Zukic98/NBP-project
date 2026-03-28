package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.UposlenikProfil;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UposlenikProfilRepository {
    private final DatabaseManager dbManager;

    public UposlenikProfilRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<UposlenikProfil> findAll() {
        List<UposlenikProfil> profili = new ArrayList<>();
        String sql = "SELECT * FROM Uposlenik_Profil";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UposlenikProfil p = new UposlenikProfil();
                p.setProfilId(rs.getLong("profil_id"));
                p.setUserId(rs.getLong("user_id"));
                p.setStanicaId(rs.getLong("stanica_id"));
                p.setBrojZnacke(rs.getString("broj_znacke"));
                p.setStatus(rs.getString("status"));
                profili.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri čitanju profila uposlenika", e);
        }
        return profili;
    }
}