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

/**
 * REST kontroler za upravljanje svjedocima vezanim za krivične slučajeve.
 *
 * <p>Nema klasnu anotaciju {@code @RequestMapping} — svaki handler definiše
 * punu putanju. Delegira poslovnu logiku servisu {@link SvjedokService}.</p>
 */
@RestController
@Tag(name = "Svjedoci", description = "Upravljanje svjedocima")
public class SvjedokController {
    private final SvjedokService service;

    /**
     * Kreira instancu kontrolera s injektovanim servisom za svjedoke.
     *
     * @param service servis za upravljanje svjedocima
     */
    public SvjedokController(SvjedokService service) {
        this.service = service;
    }

    /**
     * {@code GET /api/slucajevi/{caseId}/svjedoci} — vraća listu svjedoka
     * za zadani krivični slučaj.
     *
     * @param caseId jedinstveni identifikator krivičnog slučaja
     * @return HTTP 200 s listom svjedoka, ili HTTP 404 ako slučaj nije pronađen
     */
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
