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

/**
 * Servis za upravljanje krivičnim slučajevima.
 *
 * <p>Orkestrira {@link SlucajRepository}, {@link AdresaRepository},
 * {@link TimNaSlucajuRepository}, {@link IzvjestajRepository},
 * {@link PdfGeneratorService} i {@link DokazRepository} kako bi podržao
 * kompletan životni ciklus slučaja: kreiranje, pregled, ažuriranje statusa
 * i generisanje PDF izvještaja. Kreiranje slučaja izvršava se unutar ručno
 * upravljane JDBC transakcije.
 */
@Service
public class SlucajService {
    private final SlucajRepository slucajRepository;
    private final AdresaRepository adresaRepository;
    private final TimNaSlucajuRepository timRepository;
    private final DatabaseManager dbManager;
    private final PdfGeneratorService pdfGeneratorService;
    private final IzvjestajRepository izvjestajRepository;
    private final DokazRepository dokazRepository;

    /** Konstruktorska injekcija repozitorija slučajeva, adresa, tima, izvještaja, PDF generatora i dokaza. */
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

    /**
     * Generiše PDF izvještaj za dati slučaj i sprema ga u bazu.
     *
     * <p>Dohvata sve sekcije izvještaja (krivična djela, tim, osumnjičeni s fotografijama,
     * svjedoci, dokazi s fotografijama, lanac nadzora, forenzički izvještaji) putem
     * {@link IzvjestajRepository}, delegira generisanje PDF-a servisu {@link PdfGeneratorService},
     * a zatim sprema generisani dokument u tabelu {@code IZVJESTAJI_SLUCAJEVA}.
     *
     * @param slucajId identifikator slučaja za koji se generiše izvještaj
     * @param userId   identifikator korisnika koji generiše izvještaj (za footer i zapis)
     * @return niz bajtova generisanog PDF dokumenta
     * @throws IllegalArgumentException ako slučaj s datim ID-om ne postoji
     * @throws RuntimeException         ako dođe do greške pri generisanju PDF-a
     */
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

    /**
     * Dohvata detalje slučaja prema broju slučaja.
     *
     * @param brojSlucaja jedinstveni broj slučaja (npr. "SL-2024-001")
     * @return {@link SlucajDetaljiDTO} s kompletnim podacima o slučaju
     */
    public SlucajDetaljiDTO getSlucajDetalji(String brojSlucaja) {
        return slucajRepository.findDetaljiByBroj(brojSlucaja);
    }

    /**
     * Atomično kreira adresu, slučaj i tim na slučaju unutar jedne JDBC transakcije.
     *
     * <p>Novi slučaj dobiva inicijalni status {@code "Otvoren"}. Ako je u zahtjevu
     * proslijeđen tim, svaki član se dodaje u tabelu {@code TIM_NA_SLUCAJU}.
     * U slučaju greške transakcija se poništava (rollback).
     *
     * @param request        podaci za kreiranje slučaja (adresa, broj, opis, tim)
     * @param voditeljUserId ID korisnika koji postaje voditelj slučaja
     * @return {@link SlucajListDTO} s podacima o kreiranom slučaju i voditelju
     * @throws RuntimeException ako dođe do greške u JDBC transakciji
     */
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

    /**
     * Dohvata slučajeve na kojima je korisnik direktno angažovan (voditelj ili član tima).
     *
     * <p>Za razliku od {@link #getSlucajeviFiltered}, ova metoda vraća {@link MojSlucajDTO}
     * koji uključuje ulogu korisnika na slučaju.
     *
     * @param userId   identifikator korisnika
     * @param roleName naziv sistemske uloge korisnika (npr. {@code "INSPEKTOR"})
     * @return lista {@link MojSlucajDTO} objekata za datog korisnika
     */
    public List<MojSlucajDTO> getMojiSlucajevi(Long userId, String roleName) {
        return slucajRepository.findMojiSlucajevi(userId, roleName);
    }

    /**
     * Dohvata filtrirani spisak slučajeva prema ulozi korisnika.
     *
     * <p>Filtriranje se vrši na nivou repozitorija — npr. {@code SEF_STANICE} vidi
     * sve slučajeve svoje stanice, dok {@code INSPEKTOR} vidi samo slučajeve na
     * kojima je voditelj ili član tima.
     *
     * @param userId   identifikator korisnika
     * @param roleName naziv sistemske uloge korisnika
     * @return lista {@link SlucajListDTO} objekata prema pravilima filtriranja
     */
    public List<SlucajListDTO> getSlucajeviFiltered(Long userId, String roleName) {
        return slucajRepository.findAllFiltered(userId, roleName);
    }

    /**
     * Dohvata slučaj prema internom ID-u.
     *
     * @param id interni identifikator slučaja
     * @return {@link Optional} s {@link SlucajListDTO} ako slučaj postoji
     */
    public Optional<SlucajListDTO> getSlucajById(Long id) {
        return slucajRepository.findByIdWithVoditelj(id);
    }

    /**
     * Ažurira status slučaja.
     *
     * <p>Dozvoljeni statusi su: {@code "Otvoren"}, {@code "Zatvoren"} i {@code "Arhiviran"}.
     * Svaki drugi status uzrokuje {@link IllegalArgumentException}.
     *
     * @param id     identifikator slučaja
     * @param status novi status slučaja
     * @return {@code true} ako je ažuriranje uspješno izvršeno
     * @throws IllegalArgumentException ako je proslijeđeni status nevalidan
     */
    public boolean updateSlucajStatus(Long id, String status) {
        Set<String> allowedStatuses = Set.of("Otvoren", "Zatvoren", "Arhiviran");
        if (status == null || !allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Neispravan status. Dozvoljeno: Otvoren, Zatvoren, Arhiviran.");
        }
        return slucajRepository.updateStatus(id, status);
    }

    /**
     * Dohvata djelimični izvještaj o slučaju (bez forenzičkih izvještaja i osumnjičenih).
     *
     * <p>Sadrži: osnovne podatke o slučaju, dokaze, lanac nadzora, tim i svjedoke.
     *
     * @param slucajId identifikator slučaja
     * @return {@link Optional} s {@link IzvjestajDTO} ako slučaj postoji
     */
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
