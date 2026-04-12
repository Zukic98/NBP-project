package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.IzvjestajDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IzvjestajRepository {

    private final DatabaseManager dbManager;

    public IzvjestajRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Optional<IzvjestajDTO.SlucajInfo> findSlucajInfo(Long slucajId) {
        String sql = "SELECT s.BROJ_SLUCAJA, s.STATUS, s.OPIS, s.DATUM_KREIRANJA, " +
                "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS VODITELJ " +
                "FROM SLUCAJEVI s " +
                "JOIN nbp.NBP_USER u ON s.VODITELJ_USER_ID = u.ID " +
                "WHERE s.SLUCAJ_ID = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    IzvjestajDTO.SlucajInfo info = new IzvjestajDTO.SlucajInfo();
                    info.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
                    info.setStatus(rs.getString("STATUS"));
                    info.setOpis(rs.getString("OPIS"));
                    info.setVoditeljSlucaja(rs.getString("VODITELJ"));
                    info.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
                    return Optional.of(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case report header for case ID: " + slucajId, e);
        }

        return Optional.empty();
    }

    public List<IzvjestajDTO.DokazInfo> findDokazi(Long slucajId) {
        String sql = "SELECT d.OPIS, d.TIP_DOKAZA, d.LOKACIJA_PRONALASKA, d.DATUM_PRIKUPA, " +
                "(u.FIRST_NAME || ' ' || u.LAST_NAME) AS PRIKUPIO_IME " +
                "FROM DOKAZI d " +
                "LEFT JOIN nbp.NBP_USER u ON d.PRIKUPIO_USER_ID = u.ID " +
                "WHERE d.SLUCAJ_ID = ?";

        List<IzvjestajDTO.DokazInfo> rezultat = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.DokazInfo info = new IzvjestajDTO.DokazInfo();
                    info.setOpis(rs.getString("OPIS"));
                    info.setTipDokaza(rs.getString("TIP_DOKAZA"));
                    info.setLokacijaPronalaska(rs.getString("LOKACIJA_PRONALASKA"));
                    info.setPrikupioIme(rs.getString("PRIKUPIO_IME"));
                    info.setDatumPrikupa(rs.getTimestamp("DATUM_PRIKUPA"));
                    rezultat.add(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching case evidence report for case ID: " + slucajId, e);
        }

        return rezultat;
    }

    public List<IzvjestajDTO.LanacInfo> findLanacNadzora(Long slucajId) {
        String sql = "SELECT d.OPIS AS DOKAZ_OPIS, " +
                "(predao.FIRST_NAME || ' ' || predao.LAST_NAME) AS PREDAO_IME, " +
                "(preuzeo.FIRST_NAME || ' ' || preuzeo.LAST_NAME) AS PREUZEO_IME, " +
                "l.DATUM_PRIMOPREDAJE, l.POTVRDA_STATUS " +
                "FROM LANAC_NADZORA l " +
                "JOIN DOKAZI d ON l.DOKAZ_ID = d.DOKAZ_ID " +
                "LEFT JOIN nbp.NBP_USER predao ON l.PREDAO_USER_ID = predao.ID " +
                "LEFT JOIN nbp.NBP_USER preuzeo ON l.PREUZEO_USER_ID = preuzeo.ID " +
                "WHERE d.SLUCAJ_ID = ?";

        List<IzvjestajDTO.LanacInfo> rezultat = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.LanacInfo info = new IzvjestajDTO.LanacInfo();
                    info.setDokazOpis(rs.getString("DOKAZ_OPIS"));
                    info.setPredaoIme(rs.getString("PREDAO_IME"));
                    info.setPreuzeoIme(rs.getString("PREUZEO_IME"));
                    info.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));
                    info.setPotvrdaStatus(rs.getString("POTVRDA_STATUS"));
                    rezultat.add(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching chain-of-custody report for case ID: " + slucajId, e);
        }

        return rezultat;
    }

    public List<IzvjestajDTO.TimInfo> findTim(Long slucajId) {
        String sql = "SELECT (u.FIRST_NAME || ' ' || u.LAST_NAME) AS IME_PREZIME, " +
                "r.NAME AS NAZIV_ULOGE, t.ULOGA_NA_SLUCAJU, u.EMAIL " +
                "FROM TIM_NA_SLUCAJU t " +
                "JOIN nbp.NBP_USER u ON t.USER_ID = u.ID " +
                "LEFT JOIN nbp.NBP_ROLE r ON u.ROLE_ID = r.ID " +
                "WHERE t.SLUCAJ_ID = ?";

        List<IzvjestajDTO.TimInfo> rezultat = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.TimInfo info = new IzvjestajDTO.TimInfo();
                    info.setImePrezime(rs.getString("IME_PREZIME"));
                    info.setNazivUloge(rs.getString("NAZIV_ULOGE"));
                    info.setUlogaNaSlucaju(rs.getString("ULOGA_NA_SLUCAJU"));
                    info.setEmail(rs.getString("EMAIL"));
                    rezultat.add(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching team report for case ID: " + slucajId, e);
        }

        return rezultat;
    }

    public List<IzvjestajDTO.SvjedokInfo> findSvjedoci(Long slucajId) {
        String sql = "SELECT s.IME_PREZIME, s.JMBG, " +
                "(a.ULICA_I_BROJ || ', ' || a.GRAD) AS ADRESA, " +
                "s.KONTAKT_TELEFON, s.BILJESKA " +
                "FROM SVJEDOCI s " +
                "LEFT JOIN ADRESE a ON s.ADRESA_ID = a.ADRESA_ID " +
                "WHERE s.SLUCAJ_ID = ?";

        List<IzvjestajDTO.SvjedokInfo> rezultat = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    IzvjestajDTO.SvjedokInfo info = new IzvjestajDTO.SvjedokInfo();
                    info.setImePrezime(rs.getString("IME_PREZIME"));
                    info.setJmbg(rs.getString("JMBG"));
                    info.setAdresa(rs.getString("ADRESA"));
                    info.setKontaktTelefon(rs.getString("KONTAKT_TELEFON"));
                    info.setBiljeska(rs.getString("BILJESKA"));
                    rezultat.add(info);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching witness report for case ID: " + slucajId, e);
        }

        return rezultat;
    }
}
