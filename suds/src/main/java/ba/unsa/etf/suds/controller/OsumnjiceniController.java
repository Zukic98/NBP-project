package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/osumnjiceni")
@Tag(name = "Osumnjičeni", description = "Upravljanje osumnjičenima")
public class OsumnjiceniController {
    private final OsumnjiceniService service;

    public OsumnjiceniController(OsumnjiceniService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dohvati sve osumnjičene")
    @ApiResponse(responseCode = "200", description = "Lista osumnjičenih vraćena")
    public ResponseEntity<List<Osumnjiceni>> getAll() {
        return ResponseEntity.ok(service.getAllOsumnjiceni());
    }
}
