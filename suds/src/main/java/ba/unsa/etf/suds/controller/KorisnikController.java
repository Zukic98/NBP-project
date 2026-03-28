package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.InspektorDTO;
import ba.unsa.etf.suds.service.KorisnikService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/inspektori")
public class KorisnikController {

    private final KorisnikService korisnikService;

    public KorisnikController(KorisnikService korisnikService) {
        this.korisnikService = korisnikService;
    }

    @GetMapping
    public ResponseEntity<List<InspektorDTO>> getAll() {
        List<InspektorDTO> inspektori = korisnikService.getAllInspektori();
        return ResponseEntity.ok(inspektori);
    }
}