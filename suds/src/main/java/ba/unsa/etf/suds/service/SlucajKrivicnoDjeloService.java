package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.SlucajKrivicnoDjelo;
import ba.unsa.etf.suds.repository.SlucajKrivicnoDjeloRepository;
import ba.unsa.etf.suds.repository.KrivicnoDjeloRepository;
import ba.unsa.etf.suds.repository.SlucajRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlucajKrivicnoDjeloService {
    private final SlucajKrivicnoDjeloRepository vezaRepository;
    private final KrivicnoDjeloRepository djeloRepository;
    private final SlucajRepository slucajRepository;

    public SlucajKrivicnoDjeloService(SlucajKrivicnoDjeloRepository vezaRepository,
                                       KrivicnoDjeloRepository djeloRepository,
                                       SlucajRepository slucajRepository) {
        this.vezaRepository = vezaRepository;
        this.djeloRepository = djeloRepository;
        this.slucajRepository = slucajRepository;
    }

    /**
     * Dohvata sva krivična djela za slučaj
     */
    public List<SlucajKrivicnoDjelo> getDjelaZaSlucaj(Long slucajId) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        return vezaRepository.findBySlucajId(slucajId);
    }

    /**
     * Dodaje jedno krivično djelo na slučaj
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
     * Dodaje više krivičnih djela na slučaj odjednom
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
     * Uklanja krivično djelo sa slučaja
     */
    public void ukloniDjeloSaSlucaja(Long vezaId) {
        vezaRepository.ukloniVezu(vezaId);
    }

    /**
     * Uklanja sva krivična djela sa slučaja
     */
    public void ukloniSvaDjelaSaSlucaja(Long slucajId) {
        // Provjeri da li slučaj postoji
        slucajRepository.findById(slucajId)
                .orElseThrow(() -> new RuntimeException("Slučaj nije pronađen!"));
        
        vezaRepository.ukloniSveVezeZaSlucaj(slucajId);
    }
}