package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.model.TimNaSlucaju;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class TimNaSlucajuRepository {
    private final DatabaseManager dbManager;

    public TimNaSlucajuRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveWithConnection(Connection conn, TimNaSlucaju tim) throws SQLException {
        String sql = "INSERT INTO TIM_NA_SLUCAJU (SLUCAJ_ID, USER_ID, ULOGA_NA_SLUCAJU, DATUM_DODAVANJA) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tim.getSlucajId());
            stmt.setLong(2, tim.getUserId());
            stmt.setString(3, tim.getUlogaNaSlucaju());
            stmt.setTimestamp(4, tim.getDatumDodavanja() != null
                    ? tim.getDatumDodavanja()
                    : new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        }
    }
}
