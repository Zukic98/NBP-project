package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.SlucajKrivicnoDjelo;
import ba.unsa.etf.suds.repository.SlucajKrivicnoDjeloRepository;
import ba.unsa.etf.suds.repository.KrivicnoDjeloRepository;
import ba.unsa.etf.suds.repository.SlucajRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje vezama između slučajeva i krivičnih djela.
 *
 * <p>Orkestrira {@link SlucajKrivicnoDjeloRepository}, {@link KrivicnoDjeloRepository}
 * i {@link SlucajRepository} kako bi omogućio dodavanje, uklanjanje i pregled
 * krivičnih djela vezanih za određeni slučaj. Podržava i batch operaciju za
 * dodavanje više krivičnih djela odjednom.
 */
@Service
public class SlucajKrivicnoDjeloService {
    private final SlucajKrivicnoDjeloRepository vezaRepository;
    private final KrivicnoDjeloRepository djeloRepository;
    private final SlucajRepository slucajRepository;

    /** Konstruktorska injekcija repozitorija veza, krivičnih djela i slučajeva. */
    public SlucajKrivicnoDjeloService(SlucajKrivicnoDjeloRepository vezaRepository,
                                       KrivicnoDjeloRepository djeloRepository,
                                       SlucajRepository slucajRepository) {
        this.vezaRepository = vezaRepository;
        this.djeloRepository = djeloRepository;
        this.slucajRepository = slucajRepository;
    }

    /**
     * Dohvata sva krivična djela za slučaj.
     *
     * @param slucajId identifikator slučaja
     * @return lista {@link SlucajKrivicnoDjelo} veza za dati slučaj
     * @throws RuntimeException ako slučaj s datim ID-om ne postoji
     */
    public List<SlucajKrivicnoDjelo> getDjelaZaSlucaj(Long slucajId) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        return vezaRepository.findBySlucajId(slucajId);
    }

    /**
     * Dodaje jedno krivično djelo na slučaj.
     *
     * <p>Provjerava postojanje slučaja i krivičnog djela, te da veza između
     * njih još ne postoji. Ako veza već postoji, baca izuzetak.
     *
     * @param slucajId identifikator slučaja
     * @param djeloId  identifikator krivičnog djela
     * @return kreirani {@link SlucajKrivicnoDjelo} zapis veze
     * @throws RuntimeException ako slučaj ili krivično djelo ne postoje,
     *                          ili ako veza već postoji
     */
    public SlucajKrivicnoDjelo dodajDjeloNaSlucaj(Long slucajId, Long djeloId) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        // Provjeri da li krivično djelo postoji
        djeloRepository.findById(djeloId)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
        
        // Provjeri da li veza već postoji
        if (vezaRepository.postojiVeza(slucajId, djeloId)) {
            throw new RuntimeException("Ovo krivično djelo je već dodano na slučaj!");
        }
        
        return vezaRepository.dodajVezu(slucajId, djeloId);
    }

    /**
     * Dodaje više krivičnih djela na slučaj odjednom (batch operacija).
     *
     * <p>Provjerava postojanje slučaja i svakog krivičnog djela u listi.
     * Lista ne smije biti prazna.
     *
     * @param slucajId identifikator slučaja
     * @param djeloIds lista identifikatora krivičnih djela koja se dodaju
     * @throws RuntimeException          ako slučaj ili bilo koje krivično djelo ne postoji
     * @throws IllegalArgumentException  ako je lista {@code djeloIds} null ili prazna
     */
    public void dodajViseDjelaNaSlucaj(Long slucajId, List<Long> djeloIds) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        if (djeloIds == null || djeloIds.isEmpty()) {
            throw new IllegalArgumentException("Lista krivičnih djela je prazna.");
        }
        
        // Provjeri da li sva krivična djela postoje
        for (Long djeloId : djeloIds) {
            djeloRepository.findById(djeloId)
                    .orElseThrow(() -> new RuntimeException("Krivično djelo sa ID " + djeloId + " nije pronađeno!"));
        }
        
        vezaRepository.dodajViseVeza(slucajId, djeloIds);
    }

    /**
     * Uklanja krivično djelo sa slučaja prema ID-u veze.
     *
     * @param vezaId identifikator zapisa veze u tabeli {@code SLUCAJ_KRIVICNO_DJELO}
     */
    public void ukloniDjeloSaSlucaja(Long vezaId) {
        vezaRepository.ukloniVezu(vezaId);
    }

    /**
     * Uklanja sva krivična djela sa slučaja.
     *
     * @param slucajId identifikator slučaja čije se sve veze brišu
     * @throws RuntimeException ako slučaj s datim ID-om ne postoji
     */
    public void ukloniSvaDjelaSaSlucaja(Long slucajId) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        vezaRepository.ukloniSveVezeZaSlucaj(slucajId);
    }
}