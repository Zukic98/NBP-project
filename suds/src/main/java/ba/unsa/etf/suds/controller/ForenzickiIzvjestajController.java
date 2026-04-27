package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.service.ForenzickiIzvjestajService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forenzicki-izvjestaji")
@Tag(name = "Forenzički izvještaji")
public class ForenzickiIzvjestajController {
    private final ForenzickiIzvjestajService service;

    public ForenzickiIzvjestajController(ForenzickiIzvjestajService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('FORENZICAR')")
    public ResponseEntity<ForenzickiIzvjestaj> kreiraj(@RequestBody ForenzickiIzvjestaj izvjestaj) {
        ForenzickiIzvjestaj novi = service.kreirajIzvjestaj(izvjestaj);
        return ResponseEntity.ok(novi);
    }

    @GetMapping("/dokaz/{id}")
    public ResponseEntity<ForenzickiIzvjestaj> getByDokaz(@PathVariable Long id) {
        ForenzickiIzvjestaj izvjestaj = service.getIzvjestajZaDokaz(id);
        if (izvjestaj == null) {
        return ResponseEntity.noContent().build();
    }
    
    return ResponseEntity.ok(izvjestaj);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORENZICAR')")
    public ResponseEntity<ForenzickiIzvjestaj> azuriraj(@PathVariable Long id, @RequestBody ForenzickiIzvjestaj izvjestaj) {
        izvjestaj.setIzvjestajId(id);
        ForenzickiIzvjestaj azuriran = service.azurirajIzvjestaj(izvjestaj);
        return ResponseEntity.ok(azuriran);
    }
}