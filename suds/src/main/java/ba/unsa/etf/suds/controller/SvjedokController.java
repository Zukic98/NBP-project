package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.SvjedokDTO;
import ba.unsa.etf.suds.service.SvjedokService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Svjedoci", description = "Upravljanje svjedocima")
public class SvjedokController {
    private final SvjedokService service;

    public SvjedokController(SvjedokService service) {
        this.service = service;
    }

    @GetMapping("/api/slucajevi/{caseId}/svjedoci")
    @Operation(summary = "Dohvati svjedoke za slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista svjedoka vraćena"),
            @ApiResponse(responseCode = "404", description = "Slučaj nije pronađen")
    })
    public ResponseEntity<List<SvjedokDTO>> getByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(service.getSvjedociBySlucajId(caseId));
    }
}
