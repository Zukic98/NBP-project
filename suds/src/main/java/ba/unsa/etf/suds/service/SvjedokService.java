package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajSvjedokaRequest;
import ba.unsa.etf.suds.dto.SvjedokDTO;
import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.SvjedokRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servis za upravljanje svjedocima u krivičnim slučajevima.
 *
 * <p>Orkestrira {@link SvjedokRepository} i {@link AdresaRepository} kako bi
 * atomično kreirao adresu i svjedoka unutar iste JDBC transakcije, te ih
 * direktno vezao za odgovarajući slučaj putem {@code svjedok.setSlucajId}.
 * Sve operacije koje mijenjaju više tabela izvršavaju se ručno upravljanom
 * transakcijom (setAutoCommit/commit/rollback).
 */
@Service
public class SvjedokService {
    private final SvjedokRepository svjedokRepository;
    private final AdresaRepository adresaRepository;
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija repozitorija svjedoka, adresa i menadžera konekcija. */
    public SvjedokService(SvjedokRepository svjedokRepository,
                          AdresaRepository adresaRepository,
                          DatabaseManager dbManager) {
        this.svjedokRepository = svjedokRepository;
        this.adresaRepository = adresaRepository;
        this.dbManager = dbManager;
    }

    /**
     * Dohvata listu svih svjedoka u sistemu.
     *
     * @return lista svih {@link Svjedok} zapisa iz baze
     */
    public List<Svjedok> getAllSvjedoci() {
        return svjedokRepository.findAll();
    }

    /**
     * Dohvata listu svjedoka vezanih za određeni slučaj.
     *
     * @param slucajId identifikator slučaja
     * @return lista {@link SvjedokDTO} objekata za dati slučaj
     */
    public List<SvjedokDTO> getSvjedociBySlucajId(Long slucajId) {
        return svjedokRepository.findBySlucajId(slucajId);
    }

    /**
     * Atomično kreira adresu i svjedoka te ih vezuje za dati slučaj.
     *
     * <p>Operacija se izvršava unutar ručno upravljane JDBC transakcije.
     * Polje {@code ulicaIBroj} ima prednost nad poljem {@code adresa} u zahtjevu.
     * U slučaju greške transakcija se poništava (rollback).
     *
     * @param slucajId identifikator slučaja na koji se svjedok vezuje
     * @param request  podaci o svjedoku i njegovoj adresi
     * @return kreirani {@link Svjedok} objekat
     * @throws RuntimeException ako dođe do greške u JDBC transakciji
     */
    public Svjedok dodajSvjedoka(Long slucajId, DodajSvjedokaRequest request) {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            Adresa adresa = new Adresa();
            String ulica = request.getUlicaIBroj() != null
                    ? request.getUlicaIBroj()
                    : request.getAdresa();
            adresa.setUlicaIBroj(ulica);
            adresa.setGrad(request.getGrad());
            adresa.setPostanskiBroj(request.getPostanskiBroj());
            adresa.setDrzava(request.getDrzava());
            Long adresaId = adresaRepository.saveWithConnection(conn, adresa);

            Svjedok svjedok = new Svjedok();
            svjedok.setSlucajId(slucajId);
            svjedok.setImePrezime(request.getImePrezime());
            svjedok.setJmbg(request.getJmbg());
            svjedok.setAdresaId(adresaId);
            svjedok.setKontaktTelefon(request.getKontaktTelefon());
            svjedok.setBiljeska(request.getBiljeska());
            svjedokRepository.saveWithConnection(conn, svjedok);

            conn.commit();
            return svjedok;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.addSuppressed(e); }
            }
            throw new RuntimeException("Transaction failed during witness creation", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    throw new RuntimeException("Failed to close connection", ex);
                }
            }
        }
    }
}
