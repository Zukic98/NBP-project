package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajOsumnjicenogRequest;
import ba.unsa.etf.suds.dto.OsumnjiceniDTO;
import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.OsumnjiceniRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servis za upravljanje osumnjičenim osobama u sistemu.
 *
 * <p>Orkestrira {@link OsumnjiceniRepository} i {@link AdresaRepository} kako bi
 * atomično kreirao adresu i osumnjičenog unutar iste JDBC transakcije, te ih
 * povezao s odgovarajućim slučajem. Sve operacije koje mijenjaju više tabela
 * izvršavaju se ručno upravljanom transakcijom (setAutoCommit/commit/rollback).
 */
@Service
public class OsumnjiceniService {
    private final OsumnjiceniRepository osumnjiceniRepository;
    private final AdresaRepository adresaRepository;
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija repozitorija osumnjičenih, adresa i menadžera konekcija. */
    public OsumnjiceniService(OsumnjiceniRepository osumnjiceniRepository,
                              AdresaRepository adresaRepository,
                              DatabaseManager dbManager) {
        this.osumnjiceniRepository = osumnjiceniRepository;
        this.adresaRepository = adresaRepository;
        this.dbManager = dbManager;
    }

    /**
     * Dohvata listu svih osumnjičenih u sistemu.
     *
     * @return lista svih {@link Osumnjiceni} zapisa iz baze
     */
    public List<Osumnjiceni> getAllOsumnjiceni() {
        return osumnjiceniRepository.findAll();
    }

    /**
     * Dohvata listu osumnjičenih vezanih za određeni slučaj.
     *
     * @param slucajId identifikator slučaja
     * @return lista {@link OsumnjiceniDTO} objekata za dati slučaj
     */
    public List<OsumnjiceniDTO> getOsumnjiceniBySlucajId(Long slucajId) {
    return osumnjiceniRepository.findBySlucajId(slucajId);
}

    /**
     * Atomično kreira adresu i osumnjičenog te ih vezuje za dati slučaj.
     *
     * <p>Operacija se izvršava unutar ručno upravljane JDBC transakcije:
     * najprije se upisuje adresa, zatim osumnjičeni, a potom se kreira veza
     * u tabeli {@code SLUCAJ_OSUMNJICENI}. U slučaju greške transakcija se
     * poništava (rollback).
     *
     * @param slucajId identifikator slučaja na koji se osumnjičeni vezuje
     * @param request  podaci o osumnjičenom i njegovoj adresi
     * @return kreirani {@link Osumnjiceni} objekat s dodijeljenim ID-om
     * @throws RuntimeException ako dođe do greške u JDBC transakciji
     */
    public Osumnjiceni dodajOsumnjicenog(Long slucajId, DodajOsumnjicenogRequest request) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            Adresa adresa = new Adresa();
            adresa.setUlicaIBroj(request.getUlicaIBroj());
            adresa.setGrad(request.getGrad());
            adresa.setPostanskiBroj(request.getPostanskiBroj());
            adresa.setDrzava(request.getDrzava());
            Long adresaId = adresaRepository.saveWithConnection(conn, adresa);

            Osumnjiceni osumnjiceni = new Osumnjiceni();
            osumnjiceni.setImePrezime(request.getImePrezime());
            osumnjiceni.setJmbg(request.getJmbg());
            osumnjiceni.setAdresaId(adresaId);
            osumnjiceni.setDatumRodjenja(request.getDatumRodjenja());
            Long osumnjiceniId = osumnjiceniRepository.saveWithConnection(conn, osumnjiceni);

            osumnjiceniRepository.linkToSlucaj(conn, slucajId, osumnjiceniId);

            conn.commit();
            osumnjiceni.setOsumnjiceniId(osumnjiceniId);
            return osumnjiceni;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.addSuppressed(e); }
            }
            throw new RuntimeException("Transaction failed during suspect creation", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    throw new RuntimeException("Failed to close connection", ex);
                }
            }
        }
    }
}
