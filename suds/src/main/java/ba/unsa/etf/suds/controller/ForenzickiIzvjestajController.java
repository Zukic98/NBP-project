package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.service.ForenzickiIzvjestajService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST kontroler za upravljanje forenzičkim izvještajima.
 *
 * <p>Bazna putanja: {@code /api/forenzicki-izvjestaji}. Kreiranje i ažuriranje
 * izvještaja zahtijeva ulogu {@code FORENZIČAR}. Dohvat izvještaja po dokazu
 * dostupan je svim autentifikovanim korisnicima. Delegira sve operacije
 * servisu {@code ForenzickiIzvjestajService}.
 */
@RestController
@RequestMapping("/api/forenzicki-izvjestaji")
@Tag(name = "Forenzički izvještaji")
public class ForenzickiIzvjestajController {
    private final ForenzickiIzvjestajService service;

    /** Konstruktorska injekcija servisa za forenzičke izvještaje. */
    public ForenzickiIzvjestajController(ForenzickiIzvjestajService service) {
        this.service = service;
    }

    /**
     * POST /api/forenzicki-izvjestaji - kreira novi forenzički izvještaj.
     *
     * <p>Samo {@code FORENZIČAR} može kreirati izvještaje ({@code @PreAuthorize("hasRole('FORENZICAR')")}).
     *
     * @param izvjestaj tijelo zahtjeva sa podacima novog izvještaja
     * @return 200 + kreirani {@link ForenzickiIzvjestaj},
     *         403 ako korisnik nema ulogu FORENZIČAR
     */
    @PostMapping
    @PreAuthorize("hasRole('FORENZICAR')")
    public ResponseEntity<ForenzickiIzvjestaj> kreiraj(@RequestBody ForenzickiIzvjestaj izvjestaj) {
        ForenzickiIzvjestaj novi = service.kreirajIzvjestaj(izvjestaj);
        return ResponseEntity.ok(novi);
    }

    /**
     * GET /api/forenzicki-izvjestaji/dokaz/{id} - dohvata forenzički izvještaj za određeni dokaz.
     *
     * @param id identifikator dokaza
     * @return 200 + {@link ForenzickiIzvjestaj} ako postoji,
     *         204 ako za dati dokaz ne postoji izvještaj
     */
    @GetMapping("/dokaz/{id}")
    public ResponseEntity<ForenzickiIzvjestaj> getByDokaz(@PathVariable Long id) {
        ForenzickiIzvjestaj izvjestaj = service.getIzvjestajZaDokaz(id);
        if (izvjestaj == null) {
        return ResponseEntity.noContent().build();
    }
    
    return ResponseEntity.ok(izvjestaj);
    }

    /**
     * PUT /api/forenzicki-izvjestaji/{id} - ažurira postojeći forenzički izvještaj.
     *
     * <p>Samo {@code FORENZIČAR} može ažurirati izvještaje ({@code @PreAuthorize("hasRole('FORENZICAR')")}).
     *
     * @param id        identifikator izvještaja koji se ažurira
     * @param izvjestaj tijelo zahtjeva sa novim podacima izvještaja
     * @return 200 + ažurirani {@link ForenzickiIzvjestaj},
     *         403 ako korisnik nema ulogu FORENZIČAR
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORENZICAR')")
    public ResponseEntity<ForenzickiIzvjestaj> azuriraj(@PathVariable Long id, @RequestBody ForenzickiIzvjestaj izvjestaj) {
        izvjestaj.setIzvjestajId(id);
        ForenzickiIzvjestaj azuriran = service.azurirajIzvjestaj(izvjestaj);
        return ResponseEntity.ok(azuriran);
    }
}