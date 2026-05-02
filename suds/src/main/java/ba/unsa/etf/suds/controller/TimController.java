package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DodajClanaTRequest;
import ba.unsa.etf.suds.dto.TimClanDTO;
import ba.unsa.etf.suds.service.TimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje timovima na krivičnim slučajevima.
 *
 * <p>Nema klasnu anotaciju {@code @RequestMapping} — svaki handler definiše
 * punu putanju. Delegira poslovnu logiku servisu {@link TimService}.</p>
 */
@RestController
@Tag(name = "Tim", description = "Upravljanje timom na slučaju")
public class TimController {

    private final TimService timService;

    /**
     * Kreira instancu kontrolera s injektovanim servisom za timove.
     *
     * @param timService servis za upravljanje timovima na slučajevima
     */
    public TimController(TimService timService) {
        this.timService = timService;
    }

    /**
     * {@code GET /api/slucajevi/{caseId}/tim} — vraća listu članova tima
     * dodijeljenih zadanom krivičnom slučaju.
     *
     * @param caseId jedinstveni identifikator krivičnog slučaja
     * @return HTTP 200 s listom članova tima
     */
    @GetMapping("/api/slucajevi/{caseId}/tim")
    @Operation(summary = "Dohvati članove tima na slučaju")
    @ApiResponse(responseCode = "200", description = "Lista članova tima vraćena")
    public ResponseEntity<List<TimClanDTO>> getClanoviTima(@PathVariable Long caseId) {
        return ResponseEntity.ok(timService.getClanoviTima(caseId));
    }

    /**
     * {@code POST /api/slucajevi/{caseId}/tim} — dodaje novog člana tima
     * na zadani krivični slučaj.
     *
     * @param caseId  jedinstveni identifikator krivičnog slučaja
     * @param request podaci o uposleniku koji se dodaje u tim
     * @return HTTP 201 s podacima novokreirane dodjele, ili HTTP 400/500 u slučaju greške
     */
    @PostMapping("/api/slucajevi/{caseId}/tim")
    @Operation(summary = "Dodaj člana tima na slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Član tima dodan"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<TimClanDTO> dodajClanaTima(@PathVariable Long caseId,
                                                      @RequestBody DodajClanaTRequest request) {
        TimClanDTO timClanDTO = timService.dodajClanaTima(caseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(timClanDTO);
    }

    /**
     * {@code DELETE /api/tim/{dodjelaId}/ukloni} — uklanja člana tima
     * prema ID-u dodjele.
     *
     * @param dodjelaId jedinstveni identifikator dodjele člana tima
     * @return HTTP 200 bez tijela, ili HTTP 404 ako dodjela nije pronađena
     */
    @DeleteMapping("/api/tim/{dodjelaId}/ukloni")
    @Operation(summary = "Ukloni člana tima")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Član tima uklonjen"),
            @ApiResponse(responseCode = "404", description = "Dodjela nije pronađena")
    })
    public ResponseEntity<Void> ukloniClanaTima(@PathVariable Long dodjelaId) {
        timService.ukloniClanaTima(dodjelaId);
        return ResponseEntity.ok().build();
    }
}
