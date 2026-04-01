package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.service.SvjedokService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/svjedoci")
public class SvjedokController {
    private final SvjedokService service;

    public SvjedokController(SvjedokService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Svjedok>> getAll() {
        return ResponseEntity.ok(service.getAllSvjedoci());
    }
}