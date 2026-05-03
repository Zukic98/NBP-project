package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.repository.AdresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje adresama u sistemu.
 *
 * <p>Adrese su dijeljeni entitet koji se koristi na više mjesta: stanice,
 * osumnjičeni, svjedoci i uposlenik profili mogu referencirati isti zapis
 * u tabeli {@code ADRESE}. Servis orkestrira {@link AdresaRepository}.
 */
@Service
public class AdresaService {

    private final AdresaRepository adresaRepository;

    /** Konstruktorska injekcija repozitorija adresa. */
    public AdresaService(AdresaRepository adresaRepository) {
        this.adresaRepository = adresaRepository;
    }

    /**
     * Vraća listu svih adresa u sistemu.
     *
     * @return lista svih {@link Adresa} zapisa iz tabele {@code ADRESE}
     */
    public List<Adresa> getAllAdrese() {
        return adresaRepository.findAll();
    }

    /**
     * Kreira novu adresu nakon validacije obaveznih polja.
     *
     * <p>Polje {@code ulicaIBroj} je obavezno; ako je {@code null} ili prazno,
     * baca se {@link IllegalArgumentException}.
     *
     * @param adresa objekat adrese koji treba sačuvati
     * @return sačuvana {@link Adresa} sa dodijeljenim primarnim ključem
     * @throws IllegalArgumentException ako {@code ulicaIBroj} nije popunjen
     */
    public Adresa createAdresa(Adresa adresa) {
        // Validation before input
        if (adresa.getUlicaIBroj() == null || adresa.getUlicaIBroj().isEmpty()) {
            throw new IllegalArgumentException("Ulica i broj su obavezni!");
        }
        return adresaRepository.save(adresa);
    }
}