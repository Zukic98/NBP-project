package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.KrivicnoDjelo;
import ba.unsa.etf.suds.service.KrivicnoDjeloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/krivicna-djela")
public class KrivicnoDjeloController {
    private final KrivicnoDjeloService service;

    public KrivicnoDjeloController(KrivicnoDjeloService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<KrivicnoDjelo>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KrivicnoDjelo> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}