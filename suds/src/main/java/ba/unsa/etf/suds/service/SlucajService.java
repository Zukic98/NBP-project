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
import ba.unsa.etf.suds.repository.DokazRepository;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SlucajService {
    private final SlucajRepository slucajRepository;
    private final AdresaRepository adresaRepository;
    private final TimNaSlucajuRepository timRepository;
    private final DatabaseManager dbManager;
    private final PdfGeneratorService pdfGeneratorService;
    private final IzvjestajRepository izvjestajRepository;
    private final DokazRepository dokazRepository;

    // Ažuriraj konstruktor da uključi nove zavisnosti
    public SlucajService(SlucajRepository slucajRepository,
                         AdresaRepository adresaRepository,
                         TimNaSlucajuRepository timRepository,
                         IzvjestajRepository izvjestajRepository,
                         PdfGeneratorService pdfGeneratorService,
                         DokazRepository dokazRepository,
                         DatabaseManager dbManager) {
        this.slucajRepository = slucajRepository;
        this.adresaRepository = adresaRepository;
        this.timRepository = timRepository;
        this.izvjestajRepository = izvjestajRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.dbManager = dbManager;
        this.dokazRepository = dokazRepository;
    }

    public byte[] generatePdfReport(Long slucajId, Long userId) {
        // Dohvati sve podatke za izvještaj
        Optional<IzvjestajDTO.SlucajInfo> slucajInfo = izvjestajRepository.findSlucajInfo(slucajId);
        if (slucajInfo.isEmpty()) {
            throw new IllegalArgumentException("Slučaj nije pronađen!");
        }

        IzvjestajDTO izvjestaj = new IzvjestajDTO();
        izvjestaj.setSlucaj(slucajInfo.get());
        izvjestaj.setKrivicnaDjela(izvjestajRepository.findKrivicnaDjela(slucajId));
        izvjestaj.setTim(izvjestajRepository.findTim(slucajId));
        izvjestaj.setOsumnjiceni(izvjestajRepository.findOsumnjiceni(slucajId));
        izvjestaj.setSvjedoci(izvjestajRepository.findSvjedoci(slucajId));
        izvjestaj.setDokazi(izvjestajRepository.findDokazi(slucajId));
        izvjestaj.setLanacNadzora(izvjestajRepository.findLanacNadzora(slucajId));
        izvjestaj.setForenzickiIzvjestaji(izvjestajRepository.findForenzickiIzvjestaji(slucajId));

        // Dohvati ime korisnika koji generiše
        String imeGenerisao = findFullNameByUserId(userId);

        // Generiši PDF
        byte[] pdfBytes = pdfGeneratorService.generatePdf(izvjestaj, imeGenerisao);

        // Spasi u bazu
        // Dohvati stanicaId iz slučaja
        Long stanicaId = findStanicaIdBySlucajId(slucajId);

        izvjestajRepository.saveReport(
                slucajId,
                slucajInfo.get().getSlucajId(), // Ovo treba popraviti - dohvati stvarni stanicaId
                userId,
                imeGenerisao,
                pdfBytes,
                izvjestaj.getDokazi() != null ? izvjestaj.getDokazi().size() : 0,
                izvjestaj.getOsumnjiceni() != null ? izvjestaj.getOsumnjiceni().size() : 0,
                izvjestaj.getSvjedoci() != null ? izvjestaj.getSvjedoci().size() : 0,
                izvjestaj.getTim() != null ? izvjestaj.getTim().size() : 0,
                izvjestaj.getKrivicnaDjela() != null ? izvjestaj.getKrivicnaDjela().size() : 0
        );

        return pdfBytes;
    }

    private String findFullNameByUserId(Long userId) {
        String sql = "SELECT FIRST_NAME, LAST_NAME FROM nbp.NBP_USER WHERE ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (rs.getString("FIRST_NAME") != null ? rs.getString("FIRST_NAME") : "") +
                            " " +
                            (rs.getString("LAST_NAME") != null ? rs.getString("LAST_NAME") : "");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Nepoznat korisnik";
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
            slucaj.setStatus("Otvoren");
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
        Set<String> allowedStatuses = Set.of("Otvoren", "Zatvoren", "Arhiviran");
        if (status == null || !allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Neispravan status. Dozvoljeno: Otvoren, Zatvoren, Arhiviran.");
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

    private Long findStanicaIdBySlucajId(Long slucajId) {
        String sql = "SELECT STANICA_ID FROM SLUCAJEVI WHERE SLUCAJ_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, slucajId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("STANICA_ID");
                    return rs.wasNull() ? null : id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching station ID for case " + slucajId, e);
        }
        throw new IllegalArgumentException("Slučaj " + slucajId + " nije pronađen!");
    }
}
