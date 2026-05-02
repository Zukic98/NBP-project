package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.KrivicnoDjelo;
import ba.unsa.etf.suds.repository.KrivicnoDjeloRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Servis za upravljanje katalogom krivičnih djela.
 *
 * <p>Krivična djela su globalni katalog koji se koristi za označavanje slučajeva.
 * Jedinstvenost se provjerava na kombinaciji {@code UPPER(NAZIV)} i
 * {@code UPPER(KAZNENI_ZAKON_CLAN)} kako bi se spriječili duplikati bez obzira
 * na velika/mala slova. Orkestrira {@link KrivicnoDjeloRepository}.
 */
@Service
public class KrivicnoDjeloService {
    private final KrivicnoDjeloRepository repository;

    /** Konstruktorska injekcija repozitorija krivičnih djela. */
    public KrivicnoDjeloService(KrivicnoDjeloRepository repository) {
        this.repository = repository;
    }

    /**
     * Vraća listu svih krivičnih djela iz kataloga.
     *
     * @return lista svih {@link KrivicnoDjelo} zapisa
     */
    public List<KrivicnoDjelo> getAll() {
        return repository.findAll();
    }

    /**
     * Vraća krivično djelo po ID-u.
     *
     * @param id identifikator krivičnog djela
     * @return pronađeni {@link KrivicnoDjelo}
     * @throws RuntimeException ako krivično djelo sa zadanim ID-em ne postoji
     */
    public KrivicnoDjelo getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
    }


    /**
     * Kreira novo krivično djelo u katalogu.
     *
     * <p>Provjerava jedinstvenost kombinacije {@code (naziv, kazneniZakonClan)}
     * bez obzira na velika/mala slova ({@code UPPER} poređenje u bazi).
     * Oba polja su obavezna.
     *
     * @param djelo objekat krivičnog djela koji treba sačuvati
     * @return sačuvani {@link KrivicnoDjelo} sa dodijeljenim primarnim ključem
     * @throws IllegalArgumentException ako {@code naziv} ili {@code kazneniZakonClan} nisu popunjeni
     * @throws IllegalArgumentException ako kombinacija (naziv, član) već postoji u katalogu
     */
    public KrivicnoDjelo create(KrivicnoDjelo djelo) {
        // Validacije
        if (djelo.getNaziv() == null || djelo.getNaziv().trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv krivičnog djela je obavezan.");
        }
        if (djelo.getKazneniZakonClan() == null || djelo.getKazneniZakonClan().trim().isEmpty()) {
            throw new IllegalArgumentException("Član kaznenog zakona je obavezan.");
        }

        if (repository.postojiDuplikat(djelo.getNaziv(), djelo.getKazneniZakonClan())) {
        throw new IllegalArgumentException("Krivično djelo '" + djelo.getNaziv() + "' sa članom '" + djelo.getKazneniZakonClan() + "' već postoji u bazi!");
    }
        
        return repository.save(djelo);
    }

    /**
     * Ažurira postojeće krivično djelo u katalogu.
     *
     * <p>Provjerava da li krivično djelo postoji prije ažuriranja.
     * Oba polja ({@code naziv} i {@code kazneniZakonClan}) su obavezna.
     *
     * @param id    identifikator krivičnog djela koje se ažurira
     * @param djelo objekat sa novim podacima
     * @return ažurirani {@link KrivicnoDjelo}
     * @throws RuntimeException         ako krivično djelo sa zadanim ID-em ne postoji
     * @throws IllegalArgumentException ako {@code naziv} ili {@code kazneniZakonClan} nisu popunjeni
     */
    public KrivicnoDjelo update(Long id, KrivicnoDjelo djelo) {
        // Provjeri da li postoji
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
        
        djelo.setId(id);
        
        // Validacije
        if (djelo.getNaziv() == null || djelo.getNaziv().trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv krivičnog djela je obavezan.");
        }
        if (djelo.getKazneniZakonClan() == null || djelo.getKazneniZakonClan().trim().isEmpty()) {
            throw new IllegalArgumentException("Član kaznenog zakona je obavezan.");
        }
        
        return repository.update(djelo);
    }

    /**
     * Briše krivično djelo iz kataloga.
     *
     * @param id identifikator krivičnog djela koje se briše
     * @throws RuntimeException ako krivično djelo sa zadanim ID-em ne postoji
     */
    public void delete(Long id) {
        // Provjeri da li postoji
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
        
        repository.deleteById(id);
    }
}