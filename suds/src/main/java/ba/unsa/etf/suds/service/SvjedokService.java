package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajSvjedokaRequest;
import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.SvjedokRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class SvjedokService {
    private final SvjedokRepository svjedokRepository;
    private final AdresaRepository adresaRepository;
    private final DatabaseManager dbManager;

    public SvjedokService(SvjedokRepository svjedokRepository,
                          AdresaRepository adresaRepository,
                          DatabaseManager dbManager) {
        this.svjedokRepository = svjedokRepository;
        this.adresaRepository = adresaRepository;
        this.dbManager = dbManager;
    }

    public List<Svjedok> getAllSvjedoci() {
        return svjedokRepository.findAll();
    }

    public Svjedok dodajSvjedoka(Long slucajId, DodajSvjedokaRequest request) {
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
