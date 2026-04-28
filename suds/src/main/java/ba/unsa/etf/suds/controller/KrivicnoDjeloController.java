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

@RestController
@RequestMapping("/api/krivicna-djela")
@Tag(name = "Krivična djela", description = "Upravljanje krivičnim djelima")
public class KrivicnoDjeloController {
    private final KrivicnoDjeloService service;

    public KrivicnoDjeloController(KrivicnoDjeloService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dohvati sva krivična djela")
    @ApiResponse(responseCode = "200", description = "Lista krivičnih djela vraćena")
    public ResponseEntity<List<KrivicnoDjelo>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvati krivično djelo po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Krivično djelo pronađeno"),
            @ApiResponse(responseCode = "404", description = "Krivično djelo nije pronađeno")
    })
    public ResponseEntity<KrivicnoDjelo> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @PostMapping
    @Operation(summary = "Kreiraj novo krivično djelo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Krivično djelo kreirano"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci")
    })
    public ResponseEntity<KrivicnoDjelo> create(@RequestBody KrivicnoDjelo djelo) {
        return ResponseEntity.status(201).body(service.create(djelo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ažuriraj postojeće krivično djelo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Krivično djelo ažurirano"),
            @ApiResponse(responseCode = "404", description = "Krivično djelo nije pronađeno")
    })
    public ResponseEntity<KrivicnoDjelo> update(@PathVariable Long id, @RequestBody KrivicnoDjelo djelo) {
        return ResponseEntity.ok(service.update(id, djelo));
    }

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
