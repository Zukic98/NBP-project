package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazDosijeDTO;
import ba.unsa.etf.suds.dto.DokazListDTO;
import ba.unsa.etf.suds.dto.DokazStanjeDTO;
import ba.unsa.etf.suds.dto.KreirajDokazRequest;
import ba.unsa.etf.suds.dto.LanacDetaljiDTO;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Servis za upravljanje dokazima i njihovim stanjem u lancu nadzora.
 *
 * <p>Orkestrira {@link DokazRepository}, {@link LanacNadzoraRepository} i
 * {@link ForenzickiIzvjestajRepository}. Odgovoran je za kreiranje dokaza,
 * praćenje trenutnog nosioca (chain-of-custody), ažuriranje statusa i
 * sklapanje kompletnog dosijea dokaza.
 */
@Service
public class DokazService {

    private final DokazRepository dokazRepository;
    private final LanacNadzoraRepository lanacNadzoraRepository;
    private final ForenzickiIzvjestajRepository izvjestajRepository;

    /** Konstruktorska injekcija svih ovisnosti. */
    // Spring automatski injekta repozitorije
    public DokazService(DokazRepository dokazRepository,
                        LanacNadzoraRepository lanacNadzoraRepository,
                        ForenzickiIzvjestajRepository izvjestajRepository) {
        this.dokazRepository = dokazRepository;
        this.lanacNadzoraRepository = lanacNadzoraRepository;
        this.izvjestajRepository = izvjestajRepository;
    }

    /**
     * Kreira novi dokaz vezan za slučaj.
     *
     * <p>Ako {@code datumPrikupa} nije proslijeđen, automatski se postavlja
     * na trenutno sistemsko vrijeme.
     *
     * @param dokaz objekat dokaza koji treba sačuvati
     * @return sačuvani {@link Dokaz} sa dodijeljenim primarnim ključem
     */
    // Unos novog dokaza
    public Dokaz kreirajDokaz(Dokaz dokaz) {
        // Ako datum prikupa nije poslan, postavi na trenutno vrijeme
        if (dokaz.getDatumPrikupa() == null) {
            dokaz.setDatumPrikupa(new Timestamp(System.currentTimeMillis()));
        }
        return dokazRepository.save(dokaz);
    }

    /**
     * Dodaje novi korak u lanac nadzora za zadani dokaz.
     *
     * <p>ID dokaza iz URL putanje se uvijek upisuje u objekat {@code lanac}
     * kako bi se spriječilo nenamjerno vezivanje za pogrešan dokaz.
     *
     * @param dokazId identifikator dokaza
     * @param lanac   objekat primopredaje koji treba sačuvati
     * @return sačuvani {@link LanacNadzora} zapis
     */
    // Dodavanje novog koraka u lanac nadzora
    public LanacNadzora dodajULanacNadzora(Long dokazId, LanacNadzora lanac) {
        // Obavezno uvezujemo proslijeđeni ID dokaza iz URL-a sa objektom
        lanac.setDokazId(dokazId);
        return lanacNadzoraRepository.save(lanac);
    }

    /**
     * Sklapa kompletan dosije dokaza: sam dokaz, lanac nadzora i forenzički zaključak.
     *
     * @param dokazId identifikator dokaza
     * @return {@link DokazDosijeDTO} sa svim podacima vezanim za dokaz
     * @throws IllegalArgumentException ako dokaz sa zadanim ID-em ne postoji
     */
    // Sklapanje DTO objekta (traženo u zadatku)
    public DokazDosijeDTO getDokazDosije(Long dokazId) {
        Dokaz dokaz = dokazRepository.findById(dokazId);
        if (dokaz == null) {
            throw new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!");
        }

        DokazDosijeDTO dosije = new DokazDosijeDTO();
        dosije.setDokaz(dokaz);
        dosije.setLanacNadzora(lanacNadzoraRepository.findByDokazId(dokazId));
        dosije.setForenzickiZakljucak(izvjestajRepository.findZakljucakByDokazId(dokazId));

        return dosije;
    }

    /**
     * Vraća listu dokaza za zadani slučaj.
     *
     * @param slucajId identifikator slučaja
     * @return lista {@link DokazListDTO} objekata za dati slučaj
     */
    public List<DokazListDTO> getBySlucajId(Long slucajId) {
        return dokazRepository.findBySlucajId(slucajId);
    }

    /**
     * Kreira novi dokaz za zadani slučaj i automatski određuje stanicu na osnovu uposlenika.
     *
     * <p>Status novog dokaza se postavlja na {@code 'Odobren'}, a datum prikupa
     * na trenutno sistemsko vrijeme. Stanica se određuje iz profila uposlenika
     * koji kreira dokaz.
     *
     * @param slucajId identifikator slučaja
     * @param request  DTO sa opisom, lokacijom pronalaska i tipom dokaza
     * @param userId   identifikator uposlenika koji kreira dokaz
     * @return {@link DokazListDTO} novokreiranog dokaza
     * @throws IllegalArgumentException ako uposlenik nema profil ili stanica nije pronađena
     */
    public DokazListDTO kreirajZaSlucaj(Long slucajId, KreirajDokazRequest request, Long userId) {
        Long stanicaId = dokazRepository.findStanicaIdByUserId(userId);
        if (stanicaId == null) {
            throw new IllegalArgumentException("Employee profile not found for user ID: " + userId);
        }

        Dokaz dokaz = new Dokaz();
        dokaz.setSlucajId(slucajId);
        dokaz.setStanicaId(stanicaId);
        dokaz.setOpis(request.getOpis());
        dokaz.setLokacijaPronalaska(request.getLokacijaPronalaska());
        dokaz.setTipDokaza(request.getTipDokaza());
        dokaz.setStatus("Odobren");
        dokaz.setDatumPrikupa(new Timestamp(System.currentTimeMillis()));
        dokaz.setPrikupioUserId(userId);

        Dokaz sacuvan = dokazRepository.save(dokaz);

        return dokazRepository.findBySlucajId(slucajId).stream()
                .filter(dto -> dto.getDokazId().equals(sacuvan.getDokazId()))
                .findFirst()
                .orElseGet(() -> {
                    DokazListDTO fallback = new DokazListDTO();
                    fallback.setDokazId(sacuvan.getDokazId());
                    fallback.setOpis(sacuvan.getOpis());
                    fallback.setLokacijaPronalaska(sacuvan.getLokacijaPronalaska());
                    fallback.setTipDokaza(sacuvan.getTipDokaza());
                    fallback.setStatus(sacuvan.getStatus());
                    fallback.setDatumPrikupa(sacuvan.getDatumPrikupa());
                    fallback.setSlucajId(sacuvan.getSlucajId());
                    return fallback;
                });
    }

    /**
     * Vraća trenutno stanje dokaza: ko ga drži i da li može biti proslijeđen.
     *
     * <p>Polje {@code mozePredati} je {@code true} samo ako nema čekajuće
     * potvrde i trenutni nosilac je upravo prijavljeni korisnik ({@code userId}).
     *
     * @param dokazId identifikator dokaza
     * @param userId  identifikator prijavljenog korisnika
     * @return {@link DokazStanjeDTO} sa informacijom o trenutnom nosiocu i mogućnosti predaje
     * @throws IllegalArgumentException ako dokaz sa zadanim ID-em ne postoji
     */
    public DokazStanjeDTO getStanje(Long dokazId, Long userId) {
        DokazRepository.DokazStanjeInfo stanje = dokazRepository.findStanje(dokazId)
                .orElseThrow(() -> new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!"));

        DokazStanjeDTO dto = new DokazStanjeDTO();
        DokazStanjeDTO.TrenutniNosilacInfo nosilac = new DokazStanjeDTO.TrenutniNosilacInfo();
        nosilac.setTrenutniNosilacId(stanje.trenutniNosilacId());
        nosilac.setTrenutniNosilacIme(stanje.trenutniNosilacIme());
        nosilac.setStatus(stanje.status());
        nosilac.setZadnjaPrimopredaja(stanje.zadnjaPrimopredaja());

        dto.setTrenutniNosilac(nosilac);
        dto.setMozePredati(!stanje.imaCekajucuPotvrdu()
                && stanje.trenutniNosilacId() != null
                && stanje.trenutniNosilacId().equals(userId));

        return dto;
    }

    /**
     * Vraća detalje lanca nadzora za dokaz, uključujući imena pošiljaoca i primaoca.
     *
     * @param dokazId identifikator dokaza
     * @return lista {@link LanacDetaljiDTO} sa imenima učesnika primopredaje
     */
    public List<LanacDetaljiDTO> getLanacWithNames(Long dokazId) {
        return dokazRepository.findLanacWithNames(dokazId);
    }

    /**
     * Ažurira status dokaza.
     *
     * @param dokazId identifikator dokaza
     * @param status  novi status dokaza (npr. {@code 'Odobren'}, {@code 'Na analizi'})
     * @throws IllegalArgumentException ako dokaz sa zadanim ID-em ne postoji
     */
    public void azurirajStatus(Long dokazId, String status) {
        boolean updated = dokazRepository.updateStatus(dokazId, status);
        if (!updated) {
            throw new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!");
        }
    }
}
