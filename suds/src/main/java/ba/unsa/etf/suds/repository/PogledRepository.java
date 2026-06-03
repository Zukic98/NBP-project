package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.PogledLanacNadzoraPregledDTO;
import ba.unsa.etf.suds.dto.PogledSlucajPregledDTO;
import ba.unsa.etf.suds.dto.PogledTimNaSlucajuPregledDTO;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za čitanje iz Oracle pogleda definisanih u
 * {@code src/main/resources/db/pogledi.sql}.
 *
 * <p>Svaki pogled je kreiran sa direktivom {@code WITH READ ONLY} pa su sve
 * metode striktno read-only — bilo kakav pokušaj DML-a kroz pogled bacio
 * bi {@code ORA-42399}. Konekcije se dohvataju preko
 * {@link DatabaseManager#getConnection()} i zatvaraju try-with-resources.
 */
@Repository
public class PogledRepository {

    private final DatabaseManager dbManager;

    public PogledRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Vraća sve redove pogleda {@code POGLED_SLUCAJ_PREGLED}.
     *
     * @return lista pregleda slučajeva, najnoviji prvi
     */
    public List<PogledSlucajPregledDTO> findSviPreglediSlucajeva() {
        String sql = "SELECT SLUCAJ_ID, BROJ_SLUCAJA, OPIS, STATUS, DATUM_KREIRANJA, "
                + "STANICA_ID, IME_STANICE, VODITELJ_USER_ID, VODITELJ_IME, "
                + "BROJ_DOKAZA, BROJ_OSUMNJICENIH, BROJ_SVJEDOKA, BROJ_CLANOVA_TIMA "
                + "FROM POGLED_SLUCAJ_PREGLED ORDER BY DATUM_KREIRANJA DESC";

        List<PogledSlucajPregledDTO> rezultat = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rezultat.add(mapRowToSlucajPregled(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching from POGLED_SLUCAJ_PREGLED", e);
        }
        return rezultat;
    }

    /**
     * Vraća sve redove pogleda {@code POGLED_LANAC_NADZORA_PREGLED}.
     *
     * @return lista pregleda primopredaja, najnovija prva
     */
    public List<PogledLanacNadzoraPregledDTO> findSviPreglediLancaNadzora() {
        String sql = "SELECT UNOS_ID, DOKAZ_ID, DOKAZ_OPIS, TIP_DOKAZA, "
                + "STANICA_ID, IME_STANICE, DATUM_PRIMOPREDAJE, "
                + "PREDAO_USER_ID, PREDAO_IME, PREUZEO_USER_ID, PREUZEO_IME, "
                + "SVRHA_PRIMOPREDAJE, POTVRDA_STATUS, POTVRDA_NAPOMENA, "
                + "POTVRDA_DATUM, POTVRDIO_USER_ID, POTVRDIO_IME "
                + "FROM POGLED_LANAC_NADZORA_PREGLED ORDER BY DATUM_PRIMOPREDAJE DESC";

        List<PogledLanacNadzoraPregledDTO> rezultat = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rezultat.add(mapRowToLanacNadzoraPregled(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching from POGLED_LANAC_NADZORA_PREGLED", e);
        }
        return rezultat;
    }

    /**
     * Vraća sve redove pogleda {@code POGLED_TIM_NA_SLUCAJU_PREGLED}.
     *
     * @return lista pregleda dodjela na tim, grupisano po slučaju
     */
    public List<PogledTimNaSlucajuPregledDTO> findSviPreglediTimaNaSlucaju() {
        String sql = "SELECT DODJELA_ID, SLUCAJ_ID, BROJ_SLUCAJA, STATUS_SLUCAJA, "
                + "STANICA_ID, IME_STANICE, USER_ID, CLAN_IME, "
                + "SISTEMSKA_ULOGA, ULOGA_NA_SLUCAJU "
                + "FROM POGLED_TIM_NA_SLUCAJU_PREGLED ORDER BY SLUCAJ_ID, CLAN_IME";

        List<PogledTimNaSlucajuPregledDTO> rezultat = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rezultat.add(mapRowToTimNaSlucajuPregled(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching from POGLED_TIM_NA_SLUCAJU_PREGLED", e);
        }
        return rezultat;
    }

    private PogledSlucajPregledDTO mapRowToSlucajPregled(ResultSet rs) throws SQLException {
        PogledSlucajPregledDTO dto = new PogledSlucajPregledDTO();
        dto.setSlucajId(rs.getLong("SLUCAJ_ID"));
        dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
        dto.setOpis(rs.getString("OPIS"));
        dto.setStatus(rs.getString("STATUS"));
        dto.setDatumKreiranja(rs.getTimestamp("DATUM_KREIRANJA"));
        dto.setStanicaId(rs.getLong("STANICA_ID"));
        dto.setImeStanice(rs.getString("IME_STANICE"));
        dto.setVoditeljUserId(rs.getLong("VODITELJ_USER_ID"));
        dto.setVoditeljIme(rs.getString("VODITELJ_IME"));
        dto.setBrojDokaza(rs.getLong("BROJ_DOKAZA"));
        dto.setBrojOsumnjicenih(rs.getLong("BROJ_OSUMNJICENIH"));
        dto.setBrojSvjedoka(rs.getLong("BROJ_SVJEDOKA"));
        dto.setBrojClanovaTima(rs.getLong("BROJ_CLANOVA_TIMA"));
        return dto;
    }

    private PogledLanacNadzoraPregledDTO mapRowToLanacNadzoraPregled(ResultSet rs) throws SQLException {
        PogledLanacNadzoraPregledDTO dto = new PogledLanacNadzoraPregledDTO();
        dto.setUnosId(rs.getLong("UNOS_ID"));
        dto.setDokazId(rs.getLong("DOKAZ_ID"));
        dto.setDokazOpis(rs.getString("DOKAZ_OPIS"));
        dto.setTipDokaza(rs.getString("TIP_DOKAZA"));
        dto.setStanicaId(rs.getLong("STANICA_ID"));
        dto.setImeStanice(rs.getString("IME_STANICE"));
        dto.setDatumPrimopredaje(rs.getTimestamp("DATUM_PRIMOPREDAJE"));

        long predaoUserId = rs.getLong("PREDAO_USER_ID");
        dto.setPredaoUserId(rs.wasNull() ? null : predaoUserId);
        dto.setPredaoIme(rs.getString("PREDAO_IME"));

        dto.setPreuzeoUserId(rs.getLong("PREUZEO_USER_ID"));
        dto.setPreuzeoIme(rs.getString("PREUZEO_IME"));
        dto.setSvrhaPrimopredaje(rs.getString("SVRHA_PRIMOPREDAJE"));
        dto.setPotvrdaStatus(rs.getString("POTVRDA_STATUS"));
        dto.setPotvrdaNapomena(rs.getString("POTVRDA_NAPOMENA"));
        dto.setPotvrdaDatum(rs.getTimestamp("POTVRDA_DATUM"));

        long potvrdioUserId = rs.getLong("POTVRDIO_USER_ID");
        dto.setPotvrdioUserId(rs.wasNull() ? null : potvrdioUserId);
        dto.setPotvrdioIme(rs.getString("POTVRDIO_IME"));
        return dto;
    }

    private PogledTimNaSlucajuPregledDTO mapRowToTimNaSlucajuPregled(ResultSet rs) throws SQLException {
        PogledTimNaSlucajuPregledDTO dto = new PogledTimNaSlucajuPregledDTO();
        dto.setDodjelaId(rs.getLong("DODJELA_ID"));
        dto.setSlucajId(rs.getLong("SLUCAJ_ID"));
        dto.setBrojSlucaja(rs.getString("BROJ_SLUCAJA"));
        dto.setStatusSlucaja(rs.getString("STATUS_SLUCAJA"));
        dto.setStanicaId(rs.getLong("STANICA_ID"));
        dto.setImeStanice(rs.getString("IME_STANICE"));
        dto.setUserId(rs.getLong("USER_ID"));
        dto.setClanIme(rs.getString("CLAN_IME"));
        dto.setSistemskaUloga(rs.getString("SISTEMSKA_ULOGA"));
        dto.setUlogaNaSlucaju(rs.getString("ULOGA_NA_SLUCAJU"));
        return dto;
    }
}
