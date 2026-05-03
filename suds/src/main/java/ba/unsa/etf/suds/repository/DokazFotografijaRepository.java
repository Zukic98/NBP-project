package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DokazFotografijaDTO;
import ba.unsa.etf.suds.model.DokazFotografija;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Repozitorij za rad sa tabelom DOKAZ_FOTOGRAFIJE (fotografije dokaza).
 *
 * <p>Koristi čisti JDBC bez ORM-a (pravilo predmeta NBP). Svaka metoda
 * otvara vlastitu konekciju preko {@link DatabaseManager#getConnection()}
 * i zatvara je kroz try-with-resources. SQLException-i se wrappaju
 * u RuntimeException sa engleskom porukom.
 *
 * <p>Maksimalan broj fotografija po dokazu (10) osiguran je triggerom u bazi.
 * Jednom dodana fotografija ostaje trajno — nema UPDATE/DELETE operacija.
 */
@Repository
public class DokazFotografijaRepository {

    private final DatabaseManager databaseManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public DokazFotografijaRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Sprema fotografiju za dati dokaz u tabelu DOKAZ_FOTOGRAFIJE.
     * Ako {@code redniBroj} nije proslijeđen, automatski se određuje sljedeći slobodni redni broj.
     *
     * @param dokazId   ID dokaza (FK na DOKAZI)
     * @param file      multipart fajl koji sadrži fotografiju
     * @param redniBroj željeni redni broj fotografije, ili {@code null} za automatski
     * @param userId    ID korisnika koji dodaje fotografiju
     * @param opis      tekstualni opis fotografije
     * @return sačuvani objekat {@link DokazFotografija} sa generisanim ID-om
     * @throws IOException       ako dođe do greške pri čitanju sadržaja fajla
     * @throws RuntimeException  ako dođe do SQL greške
     */
    /**
     * Spremanje fotografije za dokaz
     * Max 10 fotografija po dokazu (osigurano triggerom u bazi)
     * Nema UPDATE/DELETE - jednom dodana fotografija ostaje trajno
     */
    public DokazFotografija save(Long dokazId, MultipartFile file, Integer redniBroj,
                                 Long userId, String opis) throws IOException {
        String sql = "INSERT INTO Dokaz_Fotografije (dokaz_id, fotografija, naziv_fajla, " +
                "mime_type, velicina_fajla, redni_broj, dodao_user_id, opis_fotografije) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"FOTOGRAFIJA_ID"})) {

            stmt.setLong(1, dokazId);
            stmt.setBytes(2, file.getBytes());
            stmt.setString(3, file.getOriginalFilename());
            stmt.setString(4, file.getContentType());
            stmt.setLong(5, file.getSize());
            stmt.setInt(6, redniBroj != null ? redniBroj : getNextRedniBroj(conn, dokazId));
            stmt.setLong(7, userId);
            stmt.setString(8, opis);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    DokazFotografija fotografija = new DokazFotografija();
                    fotografija.setFotografijaId(rs.getLong(1));
                    fotografija.setDokazId(dokazId);
                    fotografija.setNazivFajla(file.getOriginalFilename());
                    fotografija.setMimeType(file.getContentType());
                    fotografija.setVelicinaFajla(file.getSize());
                    fotografija.setFotografija(file.getBytes());
                    return fotografija;
                }
            }
            throw new SQLException("Creating evidence photo failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving evidence photo", e);
        }
    }

    /**
     * Učitava sve fotografije za dati dokaz iz tabele DOKAZ_FOTOGRAFIJE.
     * Vrši JOIN sa {@code nbp.NBP_USER} radi dohvatanja imena korisnika koji je dodao fotografiju.
     * Fotografije su kodirane u Base64 za slanje frontendu.
     *
     * @param dokazId ID dokaza čije se fotografije traže
     * @return lista DTO objekata sa Base64-enkodiranim fotografijama, sortirana po rednom broju
     * @throws RuntimeException ako dođe do SQL greške
     */
    /**
     * Dobavljanje svih fotografija za dokaz
     * Vraća DTO sa Base64 enkodiranim slikama za frontend
     */
    public List<DokazFotografijaDTO> findByDokazId(Long dokazId) {
        String sql = "SELECT df.FOTOGRAFIJA_ID, df.DOKAZ_ID, df.FOTOGRAFIJA, df.NAZIV_FAJLA, " +
                "df.MIME_TYPE, df.VELICINA_FAJLA, df.REDNI_BROJ, df.DATUM_DODAVANJA, " +
                "(u.FIRST_NAME||' '||u.LAST_NAME) AS DODAO_IME, df.OPIS_FOTOGRAFIJE " +
                "FROM DOKAZ_FOTOGRAFIJE df " +
                "LEFT JOIN nbp.NBP_USER u ON df.DODAO_USER_ID = u.ID " +
                "WHERE df.DOKAZ_ID = ? " +
                "ORDER BY df.REDNI_BROJ";

        List<DokazFotografijaDTO> result = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dokazId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DokazFotografijaDTO dto = new DokazFotografijaDTO();
                    dto.setFotografijaId(rs.getLong("FOTOGRAFIJA_ID"));
                    dto.setDokazId(rs.getLong("DOKAZ_ID"));

                    // Konverzija BLOB u Base64
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
                    dto.setOpisFotografije(rs.getString("OPIS_FOTOGRAFIJE"));
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching photos for evidence ID: " + dokazId, e);
        }
        return result;
    }

    /**
     * Dobavljanje sljedećeg rednog broja za novu fotografiju
     */
    private int getNextRedniBroj(Connection conn, Long dokazId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(REDNI_BROJ), 0) + 1 FROM DOKAZ_FOTOGRAFIJE WHERE DOKAZ_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dokazId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    /**
     * Broji fotografije za dati dokaz u tabeli DOKAZ_FOTOGRAFIJE.
     *
     * @param dokazId ID dokaza
     * @return broj fotografija, ili 0 ako nema nijedne
     * @throws RuntimeException ako dođe do SQL greške
     */
    /**
     * Provjera broja fotografija za dokaz
     */
    public int countByDokazId(Long dokazId) {
        String sql = "SELECT COUNT(*) FROM DOKAZ_FOTOGRAFIJE WHERE DOKAZ_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dokazId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting photos for evidence ID: " + dokazId, e);
        }
        return 0;
    }
}