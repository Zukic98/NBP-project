package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.IzvjestajDTO;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Repository
public class IzvjestajRepository {

    private final DatabaseManager databaseManager;

    public IzvjestajRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<IzvjestajDTO.SlucajInfo> findSlucajInfo(Long slucajId) {
        String sql = """
            SELECT s.SLUCAJ_ID, s.BROJ_SLUCAJA, s.OPIS, s.STATUS, s.DATUM_KREIRANJA,
                   (u.FIRST_NAME || ' ' || u.LAST_NAME) AS VODITELJ,
                   st.IME_STANICE
            FROM SLUCAJEVI s
            JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID
            JOIN STANICE st ON s.STANICA_ID = st.STANICA_ID
            WHERE s.SLUCAJ_ID = ?
        """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    IzvjestajDTO.SlucajInfo info = new IzvjestajDTO.SlucajInfo();
                    info.setSlucajId(rs.getLong("SLUCAJ_ID"));
                    info.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
                    info.setOpis(rs.getString("OPIS"));
                    info.setStatus(rs.getString("STATUS"));
                    info.setVoditeljSlucaja(rs.getString("VODITELJ"));
                    info.setStanica(rs.getString("IME_STANICE"));
                    info.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching case info for report", e);
        }
        return Optional.empty();
    }

    public List<IzvjestajDTO.DokazInfo> findDokazi(Long slucajId) {
        String sql = """
            SELECT d.DOKAZ_ID, d.OPIS, d.TIP_DOKAZA, d.LOKACIJA_PRONALASKA, 
                   d.STATUS, d.DATUM_PRIKUPA,
                   (u.FIRST_NAME || ' ' || u.LAST_NAME) AS PRIKUPIO_IME
            FROM DOKAZI d
            LEFT JOIN nbp.NBP_USER u ON d.PRIKUPIO_USER_ID = u.ID
            WHERE d.SLUCAJ_ID = ?
            ORDER BY d.DATUM_PRIKUPA
        """;

        List<IzvjestajDTO.DokazInfo> dokazi = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.DokazInfo dokaz = new IzvjestajDTO.DokazInfo();
                    dokaz.setDokazId(rs.getLong("DOKAZ_ID"));
                    dokaz.setOpis(rs.getString("OPIS"));
                    dokaz.setTipDokaza(rs.getString("TIP_DOKAZA"));
                    dokaz.setLokacijaPronalaska(rs.getString("LOKACIJA_PRONALASKA"));
                    dokaz.setStatus(rs.getString("STATUS"));
                    dokaz.setPrikupioIme(rs.getString("PRIKUPIO_IME"));
                    dokaz.setDatumPrikupa(rs.getTimestamp("DATUM_PRIKUPA"));

                    // Dohvati fotografije za ovaj dokaz
                    dokaz.setFotografije(findFotografijeZaDokaz(dokaz.getDokazId()));

                    dokazi.add(dokaz);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching evidence for report", e);
        }
        return dokazi;
    }

    private List<String> findFotografijeZaDokaz(Long dokazId) {
        String sql = """
            SELECT FOTOGRAFIJA FROM DOKAZ_FOTOGRAFIJE 
            WHERE DOKAZ_ID = ? 
            ORDER BY REDNI_BROJ
        """;

        List<String> base64Slike = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dokazId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byte[] fotoBytes = rs.getBytes("FOTOGRAFIJA");
                    if (fotoBytes != null) {
                        base64Slike.add(Base64.getEncoder().encodeToString(fotoBytes));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching photos for evidence " + dokazId, e);
        }
        return base64Slike;
    }

    public List<IzvjestajDTO.LanacNadzoraInfo> findLanacNadzora(Long slucajId) {
        String sql = """
            SELECT ln.UNOS_ID, ln.DATUM_PRIMOPREDAJE, ln.SVRHA_PRIMOPREDAJE,
                   ln.POTVRDA_STATUS, ln.POTVRDA_NAPOMENA, ln.POTVRDA_DATUM,
                   d.OPIS AS DOKAZ_OPIS,
                   (u1.FIRST_NAME || ' ' || u1.LAST_NAME) AS PREDAO_IME,
                   (u2.FIRST_NAME || ' ' || u2.LAST_NAME) AS PREUZEO_IME,
                   (u3.FIRST_NAME || ' ' || u3.LAST_NAME) AS POTVRDIO_IME
            FROM LANAC_NADZORA ln
            JOIN DOKAZI d ON ln.DOKAZ_ID = d.DOKAZ_ID
            LEFT JOIN nbp.NBP_USER u1 ON ln.PREDAO_USER_ID = u1.ID
            LEFT JOIN nbp.NBP_USER u2 ON ln.PREUZEO_USER_ID = u2.ID
            LEFT JOIN nbp.NBP_USER u3 ON ln.POTVRDIO_USER_ID = u3.ID
            WHERE d.SLUCAJ_ID = ?
            ORDER BY ln.DATUM_PRIMOPREDAJE
        """;

        List<IzvjestajDTO.LanacNadzoraInfo> lanac = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.LanacNadzoraInfo unos = new IzvjestajDTO.LanacNadzoraInfo();
                    unos.setUnosId(rs.getLong("UNOS_ID"));
                    unos.setDokazOpis(rs.getString("DOKAZ_OPIS"));
                    unos.setPredaoIme(rs.getString("PREDAO_IME"));
                    unos.setPreuzeoIme(rs.getString("PREUZEO_IME"));
                    unos.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
                    unos.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
                    unos.setPotvrdaStatus(rs.getString("POTVRDA_STATUS"));
                    unos.setPotvrdaNapomena(rs.getString("POTVRDA_NAPOMENA"));
                    unos.setPotvrdaDatum(rs.getTimestamp("POTVRDA_DATUM"));
                    unos.setPotvrdioIme(rs.getString("POTVRDIO_IME"));
                    lanac.add(unos);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching chain of custody for report", e);
        }
        return lanac;
    }

    public List<IzvjestajDTO.TimInfo> findTim(Long slucajId) {
        String sql = """
            SELECT t.ULOGA_NA_SLUCAJU,
                   (u.FIRST_NAME || ' ' || u.LAST_NAME) AS IME_PREZIME,
                   r.NAME AS NAZIV_ULOGE, u.EMAIL, p.BROJ_ZNACKE
            FROM TIM_NA_SLUCAJU t
            JOIN nbp.NBP_USER u ON t.USER_ID = u.ID
            JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID
            LEFT JOIN UPOSLENIK_PROFIL p ON u.ID = p.USER_ID
            WHERE t.SLUCAJ_ID = ?
            ORDER BY t.ULOGA_NA_SLUCAJU
        """;

        List<IzvjestajDTO.TimInfo> tim = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.TimInfo clan = new IzvjestajDTO.TimInfo();
                    clan.setImePrezime(rs.getString("IME_PREZIME"));
                    clan.setNazivUloge(rs.getString("NAZIV_ULOGE"));
                    clan.setUlogaNaSlucaju(rs.getString("ULOGA_NA_SLUCAJU"));
                    clan.setEmail(rs.getString("EMAIL"));
                    clan.setBrojZnacke(rs.getString("BROJ_ZNACKE"));
                    tim.add(clan);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching team for report", e);
        }
        return tim;
    }

    public List<IzvjestajDTO.SvjedokInfo> findSvjedoci(Long slucajId) {
        String sql = """
            SELECT sv.IME_PREZIME, sv.JMBG, sv.KONTAKT_TELEFON, sv.BILJESKA,
                   (a.ULICA_I_BROJ || ', ' || a.GRAD || ', ' || a.POSTANSKI_BROJ || ', ' || a.DRZAVA) AS ADRESA
            FROM SVJEDOCI sv
            LEFT JOIN ADRESE a ON sv.ADRESA_ID = a.ADRESA_ID
            WHERE sv.SLUCAJ_ID = ?
            ORDER BY sv.IME_PREZIME
        """;

        List<IzvjestajDTO.SvjedokInfo> svjedoci = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.SvjedokInfo svjedok = new IzvjestajDTO.SvjedokInfo();
                    svjedok.setImePrezime(rs.getString("IME_PREZIME"));
                    svjedok.setJmbg(rs.getString("JMBG"));
                    svjedok.setAdresa(rs.getString("ADRESA"));
                    svjedok.setKontaktTelefon(rs.getString("KONTAKT_TELEFON"));
                    svjedok.setBiljeska(rs.getString("BILJESKA"));
                    svjedoci.add(svjedok);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching witnesses for report", e);
        }
        return svjedoci;
    }

    public List<IzvjestajDTO.OsumnjiceniInfo> findOsumnjiceni(Long slucajId) {
        String sql = """
            SELECT o.OSUMNJICENI_ID, o.IME_PREZIME, o.JMBG, o.DATUM_RODJENJA,
                   (a.ULICA_I_BROJ || ', ' || a.GRAD || ', ' || a.POSTANSKI_BROJ || ', ' || a.DRZAVA) AS ADRESA
            FROM OSUMNJICENI o
            JOIN SLUCAJ_OSUMNJICENI so ON o.OSUMNJICENI_ID = so.OSUMNJICENI_ID
            LEFT JOIN ADRESE a ON o.ADRESA_ID = a.ADRESA_ID
            WHERE so.SLUCAJ_ID = ?
            ORDER BY o.IME_PREZIME
        """;

        List<IzvjestajDTO.OsumnjiceniInfo> osumnjiceni = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.OsumnjiceniInfo os = new IzvjestajDTO.OsumnjiceniInfo();
                    os.setOsumnjiceniId(rs.getLong("OSUMNJICENI_ID"));
                    os.setImePrezime(rs.getString("IME_PREZIME"));
                    os.setJmbg(rs.getString("JMBG"));
                    os.setDatumRodjenja(rs.getDate("DATUM_RODJENJA"));
                    os.setAdresa(rs.getString("ADRESA"));

                    // Dohvati fotografije za ovog osumnjičenog
                    os.setFotografije(findFotografijeZaOsumnjicenog(os.getOsumnjiceniId()));

                    osumnjiceni.add(os);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching suspects for report", e);
        }
        return osumnjiceni;
    }

    private List<String> findFotografijeZaOsumnjicenog(Long osumnjiceniId) {
        String sql = """
            SELECT FOTOGRAFIJA FROM OSUMNJICENI_FOTOGRAFIJE 
            WHERE OSUMNJICENI_ID = ? 
            ORDER BY REDNI_BROJ
        """;

        List<String> base64Slike = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, osumnjiceniId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byte[] fotoBytes = rs.getBytes("FOTOGRAFIJA");
                    if (fotoBytes != null) {
                        base64Slike.add(Base64.getEncoder().encodeToString(fotoBytes));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching photos for suspect " + osumnjiceniId, e);
        }
        return base64Slike;
    }

    public List<IzvjestajDTO.KrivicnoDjeloInfo> findKrivicnaDjela(Long slucajId) {
        String sql = """
            SELECT kd.NAZIV, kd.KATEGORIJA, kd.KAZNENI_ZAKON_CLAN
            FROM SLUCAJ_KRIVICNO_DJELO skd
            JOIN KRIVICNA_DJELA kd ON skd.DJELO_ID = kd.DJELO_ID
            WHERE skd.SLUCAJ_ID = ?
            ORDER BY kd.NAZIV
        """;

        List<IzvjestajDTO.KrivicnoDjeloInfo> djela = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.KrivicnoDjeloInfo djelo = new IzvjestajDTO.KrivicnoDjeloInfo();
                    djelo.setNaziv(rs.getString("NAZIV"));
                    djelo.setKategorija(rs.getString("KATEGORIJA"));
                    djelo.setKazneniZakonClan(rs.getString("KAZNENI_ZAKON_CLAN"));
                    djela.add(djelo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching criminal offenses for report", e);
        }
        return djela;
    }

    /**
     * Spasavanje PDF izvještaja u bazu
     * Ako izvještaj već postoji za ovaj slučaj - ažuriraj ga
     */
    public void saveReport(Long slucajId, Long stanicaId, Long userId, String imeGenerisao,
                           byte[] pdfSadrzaj, int brojDokaza, int brojOsumnjicenih,
                           int brojSvjedoka, int brojClanovaTima, int brojKrivicnihDjela) {

        // Prvo provjeri da li izvještaj već postoji
        if (postojiIzvjestaj(slucajId)) {
            // Ažuriraj postojeći
            updateReport(slucajId, stanicaId, userId, imeGenerisao, pdfSadrzaj,
                    brojDokaza, brojOsumnjicenih, brojSvjedoka, brojClanovaTima, brojKrivicnihDjela);
        } else {
            // Kreiraj novi
            insertReport(slucajId, stanicaId, userId, imeGenerisao, pdfSadrzaj,
                    brojDokaza, brojOsumnjicenih, brojSvjedoka, brojClanovaTima, brojKrivicnihDjela);
        }
    }

    /**
     * Provjera da li izvještaj već postoji za dati slučaj
     */
    private boolean postojiIzvjestaj(Long slucajId) {
        String sql = "SELECT COUNT(*) FROM IZVJESTAJI_SLUCAJEVA WHERE SLUCAJ_ID = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if report exists", e);
        }
        return false;
    }

    /**
     * Insert novog izvještaja
     */
    private void insertReport(Long slucajId, Long stanicaId, Long userId, String imeGenerisao,
                              byte[] pdfSadrzaj, int brojDokaza, int brojOsumnjicenih,
                              int brojSvjedoka, int brojClanovaTima, int brojKrivicnihDjela) {
        String sql = """
        INSERT INTO IZVJESTAJI_SLUCAJEVA 
        (SLUCAJ_ID, STANICA_ID, PDF_SADRZAJ, VELICINA_PDF, GENERISAO_USER_ID, 
         IME_GENERISAO, BROJ_DOKAZA, BROJ_OSUMNJICENIH, BROJ_SVJEDOKA, 
         BROJ_CLANOVA_TIMA, BROJ_KRIVICNIH_DJELA, DATUM_GENERISANJA)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
    """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            stmt.setLong(2, stanicaId);
            stmt.setBytes(3, pdfSadrzaj);
            stmt.setLong(4, (long) pdfSadrzaj.length);
            stmt.setLong(5, userId);
            stmt.setString(6, imeGenerisao);
            stmt.setInt(7, brojDokaza);
            stmt.setInt(8, brojOsumnjicenih);
            stmt.setInt(9, brojSvjedoka);
            stmt.setInt(10, brojClanovaTima);
            stmt.setInt(11, brojKrivicnihDjela);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving PDF report to database", e);
        }
    }

    /**
     * Ažuriranje postojećeg izvještaja
     */
    private void updateReport(Long slucajId, Long stanicaId, Long userId, String imeGenerisao,
                              byte[] pdfSadrzaj, int brojDokaza, int brojOsumnjicenih,
                              int brojSvjedoka, int brojClanovaTima, int brojKrivicnihDjela) {
        String sql = """
        UPDATE IZVJESTAJI_SLUCAJEVA 
        SET PDF_SADRZAJ = ?, VELICINA_PDF = ?, GENERISAO_USER_ID = ?, 
            IME_GENERISAO = ?, BROJ_DOKAZA = ?, BROJ_OSUMNJICENIH = ?, 
            BROJ_SVJEDOKA = ?, BROJ_CLANOVA_TIMA = ?, BROJ_KRIVICNIH_DJELA = ?,
            STANICA_ID = ?, DATUM_GENERISANJA = CURRENT_TIMESTAMP
        WHERE SLUCAJ_ID = ?
    """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, pdfSadrzaj);
            stmt.setLong(2, (long) pdfSadrzaj.length);
            stmt.setLong(3, userId);
            stmt.setString(4, imeGenerisao);
            stmt.setInt(5, brojDokaza);
            stmt.setInt(6, brojOsumnjicenih);
            stmt.setInt(7, brojSvjedoka);
            stmt.setInt(8, brojClanovaTima);
            stmt.setInt(9, brojKrivicnihDjela);
            stmt.setLong(10, stanicaId);
            stmt.setLong(11, slucajId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating PDF report in database", e);
        }
    }

    /**
     * Dohvatanje forenzičkih izvještaja za sve dokaze na slučaju
     */
    public List<IzvjestajDTO.ForenzickiIzvjestajInfo> findForenzickiIzvjestaji(Long slucajId) {
        String sql = """
        SELECT fi.IZVJESTAJ_ID, fi.DOKAZ_ID, d.OPIS AS DOKAZ_OPIS,
               fi.SADRZAJ, fi.ZAKLJUCAK, fi.DATUM_KREIRANJA,
               (u.FIRST_NAME || ' ' || u.LAST_NAME) AS KREATOR_IME
        FROM FORENZICKI_IZVJESTAJI fi
        JOIN DOKAZI d ON fi.DOKAZ_ID = d.DOKAZ_ID
        LEFT JOIN nbp.NBP_USER u ON fi.KREATOR_USER_ID = u.ID
        WHERE d.SLUCAJ_ID = ?
        ORDER BY fi.DATUM_KREIRANJA
    """;

        List<IzvjestajDTO.ForenzickiIzvjestajInfo> izvjestaji = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.ForenzickiIzvjestajInfo fi = new IzvjestajDTO.ForenzickiIzvjestajInfo();
                    fi.setIzvjestajId(rs.getLong("IZVJESTAJ_ID"));
                    fi.setDokazId(rs.getLong("DOKAZ_ID"));
                    fi.setDokazOpis(rs.getString("DOKAZ_OPIS"));
                    fi.setSadrzaj(rs.getString("SADRZAJ"));
                    fi.setZakljucak(rs.getString("ZAKLJUCAK"));
                    fi.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
                    fi.setKreatorIme(rs.getString("KREATOR_IME"));
                    izvjestaji.add(fi);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching forensic reports for case " + slucajId, e);
        }
        return izvjestaji;
    }
}