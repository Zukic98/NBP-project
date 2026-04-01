package ba.unsa.etf.suds.ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.service.UposlenikService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uposlenici")
public class UposlenikController {
    private final UposlenikService service;

    public UposlenikController(UposlenikService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UposlenikDTO>> getAll() {
        return ResponseEntity.ok(service.getAllUposlenici());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UposlenikDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUposlenikById(id));
    }
}