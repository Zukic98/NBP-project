package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.IzvjestajDTO;
import ba.unsa.etf.suds.dto.KreirajSlucajRequest;
import ba.unsa.etf.suds.dto.MojSlucajDTO;
import ba.unsa.etf.suds.dto.SlucajDetaljiDTO;
import ba.unsa.etf.suds.dto.SlucajListDTO;
import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.model.TimNaSlucaju;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.IzvjestajRepository;
import ba.unsa.etf.suds.repository.SlucajRepository;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SlucajService {
    private final SlucajRepository slucajRepository;
    private final AdresaRepository adresaRepository;
    private final TimNaSlucajuRepository timRepository;
    private final IzvjestajRepository izvjestajRepository;
    private final DatabaseManager dbManager;

    public SlucajService(SlucajRepository slucajRepository,
                         AdresaRepository adresaRepository,
                         TimNaSlucajuRepository timRepository,
                         IzvjestajRepository izvjestajRepository,
                         DatabaseManager dbManager) {
        this.slucajRepository = slucajRepository;
        this.adresaRepository = adresaRepository;
        this.timRepository = timRepository;
        this.izvjestajRepository = izvjestajRepository;
        this.dbManager = dbManager;
    }

    public SlucajDetaljiDTO getSlucajDetalji(String brojSlucaja) {
        return slucajRepository.findDetaljiByBroj(brojSlucaja);
    }

    public SlucajListDTO kreirajSlucaj(KreirajSlucajRequest request, Long voditeljUserId) {
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

            Slucaj slucaj = new Slucaj();
            slucaj.setStanicaId(request.getStanicaId());
            slucaj.setBrojSlucaja(request.getBrojSlucaja());
            slucaj.setOpis(request.getOpis());
            slucaj.setStatus("Aktivan");
            slucaj.setVoditeljUserId(voditeljUserId);
            slucaj.setDatumKreiranja(new Timestamp(System.currentTimeMillis()));
            Long slucajId = slucajRepository.saveWithConnection(conn, slucaj);

            if (request.getTim() != null) {
                for (KreirajSlucajRequest.ClanTima clan : request.getTim()) {
                    TimNaSlucaju tim = new TimNaSlucaju();
                    tim.setSlucajId(slucajId);
                    tim.setUserId(clan.getUserId());
                    tim.setUlogaNaSlucaju(clan.getUloga());
                    tim.setDatumDodavanja(new Timestamp(System.currentTimeMillis()));
                    timRepository.saveWithConnection(conn, tim);
                }
            }

            conn.commit();

            return slucajRepository.findByIdWithVoditelj(slucajId)
                    .orElseThrow(() -> new RuntimeException("Failed to fetch created case with ID: " + slucajId));

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                }
            }
            throw new RuntimeException("Transaction failed during case creation, rolled back", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    throw new RuntimeException("Failed to close connection", closeEx);
                }
            }
        }
    }

    public List<MojSlucajDTO> getMojiSlucajevi(Long userId, String roleName) {
        return slucajRepository.findMojiSlucajevi(userId, roleName);
    }

    public List<SlucajListDTO> getSlucajeviFiltered(Long userId, String roleName) {
        return slucajRepository.findAllFiltered(userId, roleName);
    }

    public Optional<SlucajListDTO> getSlucajById(Long id) {
        return slucajRepository.findByIdWithVoditelj(id);
    }

    public boolean updateSlucajStatus(Long id, String status) {
        Set<String> allowedStatuses = Set.of("Aktivan", "Zatvoren", "Arhiviran");
        if (status == null || !allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Neispravan status. Dozvoljeno: Aktivan, Zatvoren, Arhiviran.");
        }
        return slucajRepository.updateStatus(id, status);
    }

    public Optional<IzvjestajDTO> getIzvjestaj(Long slucajId) {
        Optional<IzvjestajDTO.SlucajInfo> slucajInfo = izvjestajRepository.findSlucajInfo(slucajId);
        if (slucajInfo.isEmpty()) {
            return Optional.empty();
        }

        IzvjestajDTO izvjestaj = new IzvjestajDTO();
        izvjestaj.setSlucaj(slucajInfo.get());
        izvjestaj.setDokazi(izvjestajRepository.findDokazi(slucajId));
        izvjestaj.setLanacNadzora(izvjestajRepository.findLanacNadzora(slucajId));
        izvjestaj.setTim(izvjestajRepository.findTim(slucajId));
        izvjestaj.setSvjedoci(izvjestajRepository.findSvjedoci(slucajId));

        return Optional.of(izvjestaj);
    }
}
