package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.MojaPrimopredajaDTO;
import ba.unsa.etf.suds.dto.PonistiRequest;
import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.dto.PrimopredajaRequest;
import ba.unsa.etf.suds.dto.PrimopredajaZaPotvrduDTO;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Servis za upravljanje lancem nadzora (chain of custody) dokaza.
 *
 * <p>Implementira mašinu stanja primopredaje dokaza:
 * <ol>
 *   <li>{@code posaljiDokaz} / {@code kreirajPrimopredaju} — kreira novi unos sa statusom
 *       {@code 'Čeka potvrdu'}.</li>
 *   <li>{@code potvrdiIliOdbij} — primaoc postavlja status na {@code 'Potvrđeno'} ili
 *       {@code 'Odbijeno'}; samo primaoc može izvršiti ovu akciju.</li>
 *   <li>{@code ponistiPrimopredaju} — pošiljalac može poništiti primopredaju dok je u
 *       statusu {@code 'Čeka potvrdu'}.</li>
 * </ol>
 *
 * <p>Orkestrira {@link LanacNadzoraRepository} i {@link DokazRepository}.
 */
@Service
public class LanacNadzoraService {
    private final LanacNadzoraRepository lanacRepository;
    private final DokazRepository dokazRepository;

    /** Konstruktorska injekcija svih ovisnosti. */
    public LanacNadzoraService(LanacNadzoraRepository lanacRepository, DokazRepository dokazRepository) {
        this.lanacRepository = lanacRepository;
        this.dokazRepository = dokazRepository;
    }

    /**
     * Inicira primopredaju dokaza kreiranjem novog unosa u lancu nadzora.
     *
     * <p>Novi unos dobiva status {@code 'Čeka potvrdu'}. Datum primopredaje
     * se automatski postavlja na trenutno sistemsko vrijeme.
     *
     * @param request      DTO sa ID-em dokaza, stanice, primaoca i svrhom primopredaje
     * @param predaoUserId identifikator uposlenika koji šalje dokaz
     * @return sačuvani {@link LanacNadzora} zapis
     */
    public LanacNadzora posaljiDokaz(PosaljiDokazRequest request, Long predaoUserId) {
        LanacNadzora lanac = new LanacNadzora();
        lanac.setDokazId(request.getDokazId());
        lanac.setStanicaId(request.getStanicaId());
        lanac.setDatumPrimopredaje(new Timestamp(System.currentTimeMillis()));
        lanac.setPredaoUserId(predaoUserId);
        lanac.setPreuzeoUserId(request.getPrimaocUserId());
        lanac.setSvrhaPrimopredaje(request.getSvrhaPrimopredaje());
        lanac.setPotvrdaStatus("Čeka potvrdu");

        return lanacRepository.save(lanac);
    }

    /**
     * Vraća sve zahtjeve za primopredaju vezane za zadanog korisnika.
     *
     * @param userId identifikator korisnika
     * @return lista {@link LanacNadzora} zapisa gdje je korisnik pošiljalac ili primaoc
     */
    public List<LanacNadzora> getMojiZahtjevi(Long userId) {
        return lanacRepository.findZahtjeviZaKorisnika(userId);
    }

    /**
     * Prihvata primopredaju dokaza od strane primaoca.
     *
     * <p>Samo primaoc ({@code preuzeoUserId}) može potvrditi primopredaju.
     * Primopredaja mora biti u statusu {@code 'Čeka potvrdu'}.
     *
     * @param unosId        identifikator unosa u lancu nadzora
     * @param potvrdioUserId identifikator korisnika koji potvrđuje prijem
     * @throws IllegalStateException ako unos ne postoji, korisnik nije primaoc
     *                               ili je primopredaja već obrađena
     */
    public void prihvatiDokaz(Long unosId, Long potvrdioUserId) {
        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPreuzeoUserId().equals(potvrdioUserId)) {
            throw new IllegalStateException("Samo primaoc može potvrditi primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Primopredaja je već obrađena");
        }

        lanacRepository.prihvati(unosId, potvrdioUserId);
    }

    /**
     * Kreira novu primopredaju dokaza direktno iz konteksta dokaza.
     *
     * <p>Stanica se automatski preuzima iz samog dokaza. Novi unos dobiva
     * status {@code 'Čeka potvrdu'}.
     *
     * @param dokazId      identifikator dokaza koji se predaje
     * @param request      DTO sa ID-em primaoca i svrhom primopredaje
     * @param predaoUserId identifikator uposlenika koji predaje dokaz
     * @return sačuvani {@link LanacNadzora} zapis
     * @throws IllegalStateException ako dokaz sa zadanim ID-em ne postoji
     */
    public LanacNadzora kreirajPrimopredaju(Long dokazId, PrimopredajaRequest request, Long predaoUserId) {
        Dokaz dokaz = dokazRepository.findById(dokazId);
        if (dokaz == null) {
            throw new IllegalStateException("Dokaz ne postoji: " + dokazId);
        }

        LanacNadzora lanac = new LanacNadzora();
        lanac.setDokazId(dokazId);
        lanac.setStanicaId(dokaz.getStanicaId());
        lanac.setDatumPrimopredaje(new Timestamp(System.currentTimeMillis()));
        lanac.setPredaoUserId(predaoUserId);
        lanac.setPreuzeoUserId(request.getPreuzeoUposlenikId());
        lanac.setSvrhaPrimopredaje(request.getSvrha());
        lanac.setPotvrdaStatus("Čeka potvrdu");

        return lanacRepository.save(lanac);
    }

    /**
     * Vraća listu primopredaja koje čekaju potvrdu od strane zadanog korisnika.
     *
     * @param userId identifikator primaoca
     * @return lista {@link PrimopredajaZaPotvrduDTO} sa primopredajama na čekanju
     */
    public List<PrimopredajaZaPotvrduDTO> getCekaPotvrduZaMene(Long userId) {
        return lanacRepository.findPrimopredajeZaPotvrdu(userId);
    }

    /**
     * Vraća listu primopredaja koje je zadani korisnik poslao, a koje su još na čekanju.
     *
     * <p>Za svaki zapis izračunava se {@code protekloSekundi} od trenutka slanja.
     *
     * @param userId identifikator pošiljaoca
     * @return lista {@link MojaPrimopredajaDTO} sa proteklim vremenom od slanja
     */
    public List<MojaPrimopredajaDTO> getMojaSlanjaNaPotvrdi(Long userId) {
        List<MojaPrimopredajaDTO> slanja = lanacRepository.findMojaSlanjaNaPotvrdi(userId);
        long sada = System.currentTimeMillis();
        for (MojaPrimopredajaDTO dto : slanja) {
            Timestamp datum = dto.getDatumPrimopredaje();
            if (datum != null) {
                dto.setProtekloSekundi((sada - datum.getTime()) / 1000);
            } else {
                dto.setProtekloSekundi(0L);
            }
        }
        return slanja;
    }

    /**
     * Potvrđuje ili odbija primopredaju dokaza.
     *
     * <p>Dozvoljeni statusi su {@code 'Potvrđeno'} i {@code 'Odbijeno'}.
     * Samo primaoc ({@code preuzeoUserId}) može izvršiti ovu akciju.
     * Primopredaja mora biti u statusu {@code 'Čeka potvrdu'}.
     *
     * @param unosId         identifikator unosa u lancu nadzora
     * @param status         novi status: {@code 'Potvrđeno'} ili {@code 'Odbijeno'}
     * @param napomena       opcionalna napomena uz odluku
     * @param potvrdioUserId identifikator korisnika koji donosi odluku
     * @throws IllegalStateException ako status nije validan, korisnik nije primaoc
     *                               ili je primopredaja već obrađena
     */
    public void potvrdiIliOdbij(Long unosId, String status, String napomena, Long potvrdioUserId) {
        if (!"Potvrđeno".equals(status) && !"Odbijeno".equals(status)) {
            throw new IllegalStateException("Neispravan status potvrde: " + status);
        }

        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPreuzeoUserId().equals(potvrdioUserId)) {
            throw new IllegalStateException("Samo primaoc može potvrditi ili odbiti primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Primopredaja je već obrađena");
        }

        lanacRepository.potvrdiIliOdbij(unosId, status, napomena, potvrdioUserId);
    }

    /**
     * Poništava primopredaju dokaza od strane pošiljaoca.
     *
     * <p>Samo pošiljalac ({@code predaoUserId}) može poništiti primopredaju.
     * Poništavanje je moguće samo dok je primopredaja u statusu {@code 'Čeka potvrdu'}.
     *
     * @param unosId  identifikator unosa u lancu nadzora
     * @param request DTO sa razlogom poništavanja
     * @param userId  identifikator korisnika koji poništava (mora biti pošiljalac)
     * @throws IllegalStateException ako unos ne postoji, korisnik nije pošiljalac
     *                               ili primopredaja nije u statusu {@code 'Čeka potvrdu'}
     */
    public void ponistiPrimopredaju(Long unosId, PonistiRequest request, Long userId) {
        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPredaoUserId().equals(userId)) {
            throw new IllegalStateException("Samo pošiljalac može poništiti primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Moguće je poništiti samo primopredaju koja čeka potvrdu");
        }

        lanacRepository.ponisti(unosId, request.getRazlog(), userId);
    }
}
