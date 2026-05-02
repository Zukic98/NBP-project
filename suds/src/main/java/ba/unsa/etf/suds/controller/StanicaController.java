package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.RegistrationRequest; 
import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.service.StanicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje policijskim stanicama.
 *
 * <p>Izlaže resurse na putanji {@code /api/stanice} i delegira poslovnu logiku
 * servisu {@link StanicaService}.</p>
 */
@RestController
@RequestMapping("/api/stanice")
@Tag(name = "Stanice", description = "Policijske stanice")
public class StanicaController {
    private final StanicaService service;

    /**
     * Kreira instancu kontrolera s injektovanim servisom za stanice.
     *
     * @param service servis za upravljanje policijskim stanicama
     */
    public StanicaController(StanicaService service) {
        this.service = service;
    }

    /**
     * {@code GET /api/stanice} — vraća listu svih policijskih stanica.
     *
     * @return HTTP 200 s listom svih stanica
     */
    @GetMapping
    @Operation(summary = "Dohvati sve policijske stanice")
    @ApiResponse(responseCode = "200", description = "Lista stanica vraćena")
    public ResponseEntity<List<Stanica>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * {@code GET /api/stanice/{id}} — vraća policijsku stanicu s traženim ID-om.
     *
     * @param id jedinstveni identifikator stanice
     * @return HTTP 200 s podacima stanice, ili HTTP 404 ako stanica nije pronađena
     */
    @GetMapping("/{id}")
    @Operation(summary = "Dohvati policijsku stanicu po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stanica pronađena"),
            @ApiResponse(responseCode = "404", description = "Stanica nije pronađena")
    })
    public ResponseEntity<Stanica> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * {@code POST /api/stanice/register} — registruje novu policijsku stanicu zajedno
     * s njenim šefom. Endpoint je javan i ne zahtijeva autentifikaciju.
     *
     * @param request podaci za registraciju stanice i šefa stanice
     * @return HTTP 201 s porukom uspjeha, ili HTTP 400 u slučaju greške pri registraciji
     */
    @PostMapping("/register")
    @Operation(summary = "Registruj novu policijsku stanicu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stanica registrovana"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        try {
            service.registerStanica(request);
            return ResponseEntity.status(201).body("Stanica i šef su uspješno registrovani u bazu!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Greška pri registraciji: " + e.getMessage());
        }
    }
}
