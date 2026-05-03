package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.OsumnjiceniFotografijaDTO;
import ba.unsa.etf.suds.model.OsumnjiceniFotografija;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Repozitorij za upravljanje fotografijama osumnjičenih iz tabele {@code OSUMNJICENI_FOTOGRAFIJE}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class OsumnjiceniFotografijaRepository {

    private final DatabaseManager databaseManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public OsumnjiceniFotografijaRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Sprema fotografiju osumnjičenog u tabelu {@code OSUMNJICENI_FOTOGRAFIJE}.
     * Maksimalan broj fotografija po osumnjičenom (3) osiguran je triggerom u bazi.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @param file          fajl fotografije koji se sprema
     * @param redniBroj     redni broj fotografije; ako je {@code null}, automatski se određuje
     * @param userId        identifikator korisnika koji dodaje fotografiju
     * @param opis          opis fotografije
     * @return sačuvana fotografija sa generisanim ID-om
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    /**
     * Spremanje fotografije za osumnjičenog
     * Max 3 fotografije po osumnjičenom (osigurano triggerom u bazi)
     */
    public OsumnjiceniFotografija save(Long osumnjiceniId, MultipartFile file,
                                       Integer redniBroj, Long userId, String opis) throws IOException {
        String sql = "INSERT INTO OSUMNJICENI_FOTOGRAFIJE (OSUMNJICENI_ID, FOTOGRAFIJA, NAZIV_FAJLA, " +
                "MIME_TYPE, VELICINA_FAJLA, REDNI_BROJ, DODAO_USER_ID, OPIS_FOTOGRAFIJE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"FOTOGRAFIJA_ID"})) {

            stmt.setLong(1, osumnjiceniId);
            stmt.setBytes(2, file.getBytes());
            stmt.setString(3, file.getOriginalFilename());
            stmt.setString(4, file.getContentType());
            stmt.setLong(5, file.getSize());
            stmt.setInt(6, redniBroj != null ? redniBroj : getNextRedniBroj(conn, osumnjiceniId));
            stmt.setLong(7, userId);
            stmt.setString(8, opis);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    OsumnjiceniFotografija fotografija = new OsumnjiceniFotografija();
                    fotografija.setFotografijaId(rs.getLong(1));
                    fotografija.setOsumnjiceniId(osumnjiceniId);
                    fotografija.setNazivFajla(file.getOriginalFilename());
                    fotografija.setMimeType(file.getContentType());
                    fotografija.setVelicinaFajla(file.getSize());
                    fotografija.setFotografija(file.getBytes());
                    return fotografija;
                }
            }
            throw new SQLException("Creating suspect photo failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving suspect photo", e);
        }
    }

    /**
     * Dohvata sve fotografije za datog osumnjičenog sa JOIN-om na korisnike koji su ih dodali/izmijenili.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @return lista DTO-ova sa podacima o fotografijama (uključujući Base64 sadržaj)
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    /**
     * Dobavljanje svih fotografija za osumnjičenog
     */
    public List<OsumnjiceniFotografijaDTO> findByOsumnjiceniId(Long osumnjiceniId) {
        String sql = "SELECT ofp.FOTOGRAFIJA_ID, ofp.OSUMNJICENI_ID, ofp.FOTOGRAFIJA, " +
                "ofp.NAZIV_FAJLA, ofp.MIME_TYPE, ofp.VELICINA_FAJLA, ofp.REDNI_BROJ, " +
                "ofp.DATUM_DODAVANJA, (u1.FIRST_NAME||' '||u1.LAST_NAME) AS DODAO_IME, " +
                "ofp.DATUM_IZMJENE, (u2.FIRST_NAME||' '||u2.LAST_NAME) AS IZMIJENIO_IME, " +
                "ofp.OPIS_FOTOGRAFIJE, ofp.STATUS " +
                "FROM OSUMNJICENI_FOTOGRAFIJE ofp " +
                "LEFT JOIN nbp.NBP_USER u1 ON ofp.DODAO_USER_ID = u1.ID " +
                "LEFT JOIN nbp.NBP_USER u2 ON ofp.IZMIJENIO_USER_ID = u2.ID " +
                "WHERE ofp.OSUMNJICENI_ID = ? " +
                "ORDER BY ofp.REDNI_BROJ";

        List<OsumnjiceniFotografijaDTO> result = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, osumnjiceniId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OsumnjiceniFotografijaDTO dto = new OsumnjiceniFotografijaDTO();
                    dto.setFotografijaId(rs.getLong("FOTOGRAFIJA_ID"));
                    dto.setOsumnjiceniId(rs.getLong("OSUMNJICENI_ID"));

                    byte[] fotoBytes = rs.getBytes("FOTOGRAFIJA");
                    if (fotoBytes != null) {
                        dto.setFotografijaBase64(Base64.getEncoder().encodeToString(fotoBytes));
                    }

                    dto.setNazivFajla(rs.getString("NAZIV_FAJLA"));
                    dto.setMimeType(rs.getString("MIME_TYPE"));
                    dto.setVelicinaFajla(rs.getLong("VELICINA_FAJLA"));
                    dto.setRedniBroj(rs.getInt("REDNI_BROJ"));
                    dto.setDatumDodavanja(rs.getTimestamp("DATUM_DODAVANJA"));
                    dto.setDodaoIme(rs.getString("DODAO_IME"));
                    dto.setDatumIzmjene(rs.getTimestamp("DATUM_IZMJENE"));
                    dto.setIzmijenioIme(rs.getString("IZMIJENIO_IME"));
                    dto.setOpisFotografije(rs.getString("OPIS_FOTOGRAFIJE"));
                    dto.setStatus(rs.getString("STATUS"));
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching photos for suspect ID: " + osumnjiceniId, e);
        }
        return result;
    }

    /**
     * Ažurira sadržaj i metapodatke fotografije osumnjičenog.
     *
     * @param fotografijaId identifikator fotografije koja se ažurira
     * @param file          novi fajl fotografije
     * @param userId        identifikator korisnika koji vrši izmjenu
     * @param opis          novi opis fotografije
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    /**
     * Ažuriranje fotografije osumnjičenog (dozvoljeno)
     */
    public void update(Long fotografijaId, MultipartFile file, Long userId, String opis) throws IOException {
        String sql = "UPDATE OSUMNJICENI_FOTOGRAFIJE SET FOTOGRAFIJA = ?, NAZIV_FAJLA = ?, " +
                "MIME_TYPE = ?, VELICINA_FAJLA = ?, IZMIJENIO_USER_ID = ?, " +
                "DATUM_IZMJENE = CURRENT_TIMESTAMP, OPIS_FOTOGRAFIJE = ? " +
                "WHERE FOTOGRAFIJA_ID = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, file.getBytes());
            stmt.setString(2, file.getOriginalFilename());
            stmt.setString(3, file.getContentType());
            stmt.setLong(4, file.getSize());
            stmt.setLong(5, userId);
            stmt.setString(6, opis);
            stmt.setLong(7, fotografijaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating suspect photo", e);
        }
    }

    /**
     * Briše fotografiju osumnjičenog iz tabele {@code OSUMNJICENI_FOTOGRAFIJE} po ID-u.
     *
     * @param fotografijaId identifikator fotografije koja se briše
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    /**
     * Brisanje fotografije osumnjičenog (dozvoljeno)
     */
    public void delete(Long fotografijaId) {
        String sql = "DELETE FROM OSUMNJICENI_FOTOGRAFIJE WHERE FOTOGRAFIJA_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, fotografijaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting suspect photo", e);
        }
    }

    private int getNextRedniBroj(Connection conn, Long osumnjiceniId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(REDNI_BROJ), 0) + 1 FROM OSUMNJICENI_FOTOGRAFIJE WHERE OSUMNJICENI_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, osumnjiceniId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    /**
     * Broji fotografije za datog osumnjičenog.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @return broj fotografija u tabeli {@code OSUMNJICENI_FOTOGRAFIJE} za datog osumnjičenog
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public int countByOsumnjiceniId(Long osumnjiceniId) {
        String sql = "SELECT COUNT(*) FROM OSUMNJICENI_FOTOGRAFIJE WHERE OSUMNJICENI_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, osumnjiceniId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting photos for suspect ID: " + osumnjiceniId, e);
        }
        return 0;
    }
}