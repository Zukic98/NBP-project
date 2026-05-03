package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servis za upravljanje forenzičkim izvještajima.
 *
 * <p>Kreiranje i izmjena izvještaja dozvoljena je isključivo korisnicima s ulogom
 * {@code FORENZIČAR} — ovo se provjerava na nivou kontrolera putem
 * {@code @PreAuthorize}. Servis automatski upisuje ID kreatora iz
 * {@link org.springframework.security.core.context.SecurityContext}-a.
 * Orkestrira {@link ForenzickiIzvjestajRepository}.
 */
@Service
public class ForenzickiIzvjestajService {
    private final ForenzickiIzvjestajRepository repository;

    /** Konstruktorska injekcija repozitorija forenzičkih izvještaja. */
    public ForenzickiIzvjestajService(ForenzickiIzvjestajRepository repository) {
        this.repository = repository;
    }

    /**
     * Kreira novi forenzički izvještaj i automatski upisuje ID kreatora.
     *
     * <p>ID kreatora se čita iz {@link org.springframework.security.core.context.SecurityContextHolder}
     * i upisuje u polje {@code kreatorUserId}. Pristup ovoj metodi je zaštićen
     * na nivou kontrolera ({@code @PreAuthorize("hasRole('FORENZIČAR')")}).
     *
     * @param izvjestaj objekat izvještaja koji treba sačuvati
     * @return sačuvani {@link ForenzickiIzvjestaj} sa dodijeljenim primarnim ključem
     */
    public ForenzickiIzvjestaj kreirajIzvjestaj(ForenzickiIzvjestaj izvjestaj) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        izvjestaj.setKreatorUserId(Long.parseLong(principal.toString()));
        
        return repository.save(izvjestaj);
    }

    /**
     * Vraća forenzički izvještaj vezan za zadani dokaz.
     *
     * @param dokazId identifikator dokaza
     * @return {@link ForenzickiIzvjestaj} za dati dokaz, ili {@code null} ako ne postoji
     */
    public ForenzickiIzvjestaj getIzvjestajZaDokaz(Long dokazId) {
        return repository.findByDokazId(dokazId);
    }

    /**
     * Ažurira postojeći forenzički izvještaj.
     *
     * <p>Pristup ovoj metodi je zaštićen na nivou kontrolera
     * ({@code @PreAuthorize("hasRole('FORENZIČAR')")}).
     *
     * @param izvjestaj objekat izvještaja sa ažuriranim podacima (mora imati postavljen ID)
     * @return ažurirani {@link ForenzickiIzvjestaj}
     */
    public ForenzickiIzvjestaj azurirajIzvjestaj(ForenzickiIzvjestaj izvjestaj) {
    return repository.update(izvjestaj);
}
}