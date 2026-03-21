package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.service.AdresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adrese")
public class AdresaController {

    private final AdresaService adresaService;

    public AdresaController(AdresaService adresaService) {
        this.adresaService = adresaService;
    }

    // GET zahtjev za sve adrese (Pristup: http://localhost:8080/api/adrese)
    @GetMapping
    public ResponseEntity<List<Adresa>> getAllAdrese() {
        return ResponseEntity.ok(adresaService.getAllAdrese());
    }

    // POST zahtjev za novu adresu
    @PostMapping
    public ResponseEntity<Adresa> createAdresa(@RequestBody Adresa adresa) {
        Adresa kreiranaAdresa = adresaService.createAdresa(adresa);
        return ResponseEntity.ok(kreiranaAdresa);
    }
}