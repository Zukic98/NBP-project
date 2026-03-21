package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.service.StanicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stanice")
public class StanicaController {
    private final StanicaService service;

    public StanicaController(StanicaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Stanica>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stanica> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}