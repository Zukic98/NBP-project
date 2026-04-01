package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DokazDosijeDTO;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.service.DokazService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dokazi")
public class DokazController {

    private final DokazService dokazService;

    public DokazController(DokazService dokazService) {
        this.dokazService = dokazService;
    }

    // POST /api/dokazi -> Unos novog dokaza
    @PostMapping
    public ResponseEntity<Dokaz> dodajDokaz(@RequestBody Dokaz dokaz) {
        try {
            Dokaz spasenDokaz = dokazService.kreirajDokaz(dokaz);
            return ResponseEntity.status(HttpStatus.CREATED).body(spasenDokaz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/dokazi/{id}/lanac -> Dodavanje novog koraka u lanac nadzora
    @PostMapping("/{id}/lanac")
    public ResponseEntity<LanacNadzora> dodajULanacNadzora(
            @PathVariable("id") Long dokazId,
            @RequestBody LanacNadzora lanac) {
        try {
            LanacNadzora noviKorak = dokazService.dodajULanacNadzora(dokazId, lanac);
            return ResponseEntity.status(HttpStatus.CREATED).body(noviKorak);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/dokazi/{id}/dosije -> Vraća DTO sa svim podacima (korisno za provjeru i odbranu)
    @GetMapping("/{id}/dosije")
    public ResponseEntity<DokazDosijeDTO> getDosije(@PathVariable("id") Long id) {
        try {
            DokazDosijeDTO dosije = dokazService.getDokazDosije(id);
            return ResponseEntity.ok(dosije);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 ako dokaz ne postoji
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}