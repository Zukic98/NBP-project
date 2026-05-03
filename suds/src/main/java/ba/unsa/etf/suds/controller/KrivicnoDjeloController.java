package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.KrivicnoDjelo;
import ba.unsa.etf.suds.service.KrivicnoDjeloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje krivičnim djelima.
 *
 * <p>Bazna putanja: {@code /api/krivicna-djela}. Pruža CRUD operacije nad
 * krivičnim djelima. Delegira sve operacije servisu {@code KrivicnoDjeloService}.
 * Dostupno svim autentifikovanim korisnicima.
 */
@RestController
@RequestMapping("/api/krivicna-djela")
@Tag(name = "Krivična djela", description = "Upravljanje krivičnim djelima")
public class KrivicnoDjeloController {
    private final KrivicnoDjeloService service;

    /** Konstruktorska injekcija servisa za krivična djela. */
    public KrivicnoDjeloController(KrivicnoDjeloService service) {
        this.service = service;
    }

    /**
     * GET /api/krivicna-djela - dohvata sva krivična djela u sistemu.
     *
     * @return 200 + lista svih {@link KrivicnoDjelo}
     */
    @GetMapping
    @Operation(summary = "Dohvati sva krivična djela")
    @ApiResponse(responseCode = "200", description = "Lista krivičnih djela vraćena")
    public ResponseEntity<List<KrivicnoDjelo>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * GET /api/krivicna-djela/{id} - dohvata krivično djelo po ID-u.
     *
     * @param id identifikator krivičnog djela
     * @return 200 + {@link KrivicnoDjelo},
     *         404 ako krivično djelo nije pronađeno
     */
    @GetMapping("/{id}")
    @Operation(summary = "Dohvati krivično djelo po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Krivično djelo pronađeno"),
            @ApiResponse(responseCode = "404", description = "Krivično djelo nije pronađeno")
    })
    public ResponseEntity<KrivicnoDjelo> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }


    /**
     * POST /api/krivicna-djela - kreira novo krivično djelo.
     *
     * @param djelo tijelo zahtjeva sa podacima novog krivičnog djela
     * @return 201 + kreirano {@link KrivicnoDjelo},
     *         400 ako su podaci nevalidni
     */
    @PostMapping
    @Operation(summary = "Kreiraj novo krivično djelo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Krivično djelo kreirano"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci")
    })
    public ResponseEntity<KrivicnoDjelo> create(@RequestBody KrivicnoDjelo djelo) {
        return ResponseEntity.status(201).body(service.create(djelo));
    }

    /**
     * PUT /api/krivicna-djela/{id} - ažurira postojeće krivično djelo.
     *
     * @param id   identifikator krivičnog djela koje se ažurira
     * @param djelo tijelo zahtjeva sa novim podacima
     * @return 200 + ažurirano {@link KrivicnoDjelo},
     *         404 ako krivično djelo nije pronađeno
     */
    @PutMapping("/{id}")
    @Operation(summary = "Ažuriraj postojeće krivično djelo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Krivično djelo ažurirano"),
            @ApiResponse(responseCode = "404", description = "Krivično djelo nije pronađeno")
    })
    public ResponseEntity<KrivicnoDjelo> update(@PathVariable Long id, @RequestBody KrivicnoDjelo djelo) {
        return ResponseEntity.ok(service.update(id, djelo));
    }

    /**
     * DELETE /api/krivicna-djela/{id} - briše krivično djelo.
     *
     * @param id identifikator krivičnog djela koje se briše
     * @return 204 bez sadržaja,
     *         404 ako krivično djelo nije pronađeno
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Obriši krivično djelo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Krivično djelo obrisano"),
            @ApiResponse(responseCode = "404", description = "Krivično djelo nije pronađeno")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
