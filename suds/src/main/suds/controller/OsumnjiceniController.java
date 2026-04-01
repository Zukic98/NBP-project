package ba.unsa.etf.suds.ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/osumnjiceni")
public class OsumnjiceniController {
    private final OsumnjiceniService service;

    public OsumnjiceniController(OsumnjiceniService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Osumnjiceni>> getAll() {
        return ResponseEntity.ok(service.getAllOsumnjiceni());
    }
}