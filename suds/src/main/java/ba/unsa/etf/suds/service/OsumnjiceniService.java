package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajOsumnjicenogRequest;
import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.OsumnjiceniRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class OsumnjiceniService {
    private final OsumnjiceniRepository osumnjiceniRepository;
    private final AdresaRepository adresaRepository;
    private final DatabaseManager dbManager;

    public OsumnjiceniService(OsumnjiceniRepository osumnjiceniRepository,
                              AdresaRepository adresaRepository,
                              DatabaseManager dbManager) {
        this.osumnjiceniRepository = osumnjiceniRepository;
        this.adresaRepository = adresaRepository;
        this.dbManager = dbManager;
    }

    public List<Osumnjiceni> getAllOsumnjiceni() {
        return osumnjiceniRepository.findAll();
    }

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
