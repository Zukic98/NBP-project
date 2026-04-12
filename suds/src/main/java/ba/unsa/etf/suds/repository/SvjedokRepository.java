package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.SvjedokDTO;
import ba.unsa.etf.suds.model.Svjedok;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SvjedokRepository {
    private final DatabaseManager dbManager;

    public SvjedokRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void saveWithConnection(Connection conn, Svjedok svjedok) throws SQLException {
        String sql = "INSERT INTO Svjedoci (SLUCAJ_ID, IME_PREZIME, JMBG, ADRESA_ID, KONTAKT_TELEFON, BILJESKA) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, svjedok.getSlucajId());
            stmt.setString(2, svjedok.getImePrezime());
            stmt.setString(3, svjedok.getJmbg());
            stmt.setLong(4, svjedok.getAdresaId());
            stmt.setString(5, svjedok.getKontaktTelefon());
            stmt.setString(6, svjedok.getBiljeska());
            stmt.executeUpdate();
        }
    }

    public List<Svjedok> findAll() {
        List<Svjedok> svjedoci = new ArrayList<>();
        String sql = "SELECT * FROM Svjedoci";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Svjedok svjedok = new Svjedok();
                svjedok.setSvjedokId(rs.getLong("svjedok_id"));
                svjedok.setSlucajId(rs.getLong("slucaj_id"));
                svjedok.setImePrezime(rs.getString("ime_prezime"));
                svjedok.setJmbg(rs.getString("jmbg"));
                svjedok.setAdresaId(rs.getLong("adresa_id"));
                svjedok.setKontaktTelefon(rs.getString("kontakt_telefon"));
                svjedok.setBiljeska(rs.getString("biljeska"));
                svjedoci.add(svjedok);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju svjedoka", e);
        }
        return svjedoci;
    }

    public List<SvjedokDTO> findBySlucajId(Long slucajId) {
        List<SvjedokDTO> svjedoci = new ArrayList<>();
        String sql = "SELECT sv.SVJEDOK_ID, sv.IME_PREZIME, sv.KONTAKT_TELEFON, " +
                "(a.ULICA_I_BROJ || ', ' || a.GRAD) AS ADRESA, sv.JMBG, sv.BILJESKA " +
                "FROM SVJEDOCI sv LEFT JOIN ADRESE a ON sv.ADRESA_ID = a.ADRESA_ID " +
                "WHERE sv.SLUCAJ_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, slucajId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    svjedoci.add(new SvjedokDTO(
                            rs.getLong("SVJEDOK_ID"),
                            rs.getString("IME_PREZIME"),
                            rs.getString("KONTAKT_TELEFON"),
                            rs.getString("ADRESA"),
                            rs.getString("JMBG"),
                            rs.getString("BILJESKA")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching witnesses by case id", e);
        }

        return svjedoci;
    }

    public Long save(Long slucajId,
                     String imePrezime,
                     String jmbg,
                     Long adresaId,
                     String kontaktTelefon,
                     String biljeska) {
        String sql = "INSERT INTO SVJEDOCI (SLUCAJ_ID, IME_PREZIME, JMBG, ADRESA_ID, KONTAKT_TELEFON, BILJESKA) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"SVJEDOK_ID"})) {
            stmt.setLong(1, slucajId);
            stmt.setString(2, imePrezime);
            stmt.setString(3, jmbg);
            stmt.setLong(4, adresaId);
            stmt.setString(5, kontaktTelefon);
            stmt.setString(6, biljeska);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }

            throw new RuntimeException("No generated key returned for Svjedok insert");
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating witness", e);
        }
    }
}
