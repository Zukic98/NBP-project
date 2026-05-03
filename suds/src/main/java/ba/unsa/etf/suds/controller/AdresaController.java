package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.service.AdresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje adresama.
 *
 * <p>Bazna putanja: {@code /api/adrese}. Delegira sve operacije servisu
 * {@code AdresaService}. Endpoint je dostupan svim autentifikovanim korisnicima.
 */
@RestController
@RequestMapping("/api/adrese")
@Tag(name = "Adrese", description = "Upravljanje adresama")
public class AdresaController {

    private final AdresaService adresaService;

    /** Konstruktorska injekcija servisa za adrese. */
    public AdresaController(AdresaService adresaService) {
        this.adresaService = adresaService;
    }

    /**
     * GET /api/adrese - dohvata sve adrese u sistemu.
     *
     * @return 200 + lista svih adresa
     */
    // GET zahtjev za sve adrese (Pristup: http://localhost:8080/api/adrese)
    @GetMapping
    @Operation(summary = "Dohvati sve adrese")
    @ApiResponse(responseCode = "200", description = "Lista adresa vraćena")
    public ResponseEntity<List<Adresa>> getAllAdrese() {
        return ResponseEntity.ok(adresaService.getAllAdrese());
    }

    /**
     * POST /api/adrese - kreira novu adresu.
     *
     * @param adresa tijelo zahtjeva sa podacima adrese
     * @return 200 + kreirana adresa, 400 ako su podaci neispravni, 500 pri grešci na serveru
     */
    // POST zahtjev za novu adresu
    @PostMapping
    @Operation(summary = "Kreiraj novu adresu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Adresa kreirana"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<Adresa> createAdresa(@RequestBody Adresa adresa) {
        Adresa kreiranaAdresa = adresaService.createAdresa(adresa);
        return ResponseEntity.ok(kreiranaAdresa);
    }
}
