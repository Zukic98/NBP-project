package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.SlucajKrivicnoDjelo;
import ba.unsa.etf.suds.service.SlucajKrivicnoDjeloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slucajevi/{slucajId}/krivicna-djela")
@Tag(name = "Krivična djela na slučaju", description = "Upravljanje krivičnim djelima na konkretnom slučaju")
public class SlucajKrivicnoDjeloController {
    private final SlucajKrivicnoDjeloService service;

    public SlucajKrivicnoDjeloController(SlucajKrivicnoDjeloService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dohvati sva krivična djela za slučaj")
    @ApiResponse(responseCode = "200", description = "Lista krivičnih djela vraćena")
    public ResponseEntity<List<SlucajKrivicnoDjelo>> getDjelaZaSlucaj(@PathVariable Long slucajId) {
        return ResponseEntity.ok(service.getDjelaZaSlucaj(slucajId));
    }

    @PostMapping
    @Operation(summary = "Dodaj krivično djelo na slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Krivično djelo dodano na slučaj"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci"),
            @ApiResponse(responseCode = "409", description = "Već postoji veza")
    })
    public ResponseEntity<SlucajKrivicnoDjelo> dodajDjeloNaSlucaj(
            @PathVariable Long slucajId,
            @RequestBody Map<String, Long> body) {
        Long djeloId = body.get("djeloId");
        return ResponseEntity.status(201).body(service.dodajDjeloNaSlucaj(slucajId, djeloId));
    }

    @PostMapping("/batch")
    @Operation(summary = "Dodaj više krivičnih djela na slučaj odjednom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Krivična djela dodana na slučaj"),
            @ApiResponse(responseCode = "400", description = "Nevalidni podaci")
    })
    public ResponseEntity<Void> dodajViseDjelaNaSlucaj(
            @PathVariable Long slucajId,
            @RequestBody Map<String, List<Long>> body) {
        List<Long> djeloIds = body.get("djeloIds");
        service.dodajViseDjelaNaSlucaj(slucajId, djeloIds);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{vezaId}")
    @Operation(summary = "Ukloni krivično djelo sa slučaja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Krivično djelo uklonjeno sa slučaja"),
            @ApiResponse(responseCode = "404", description = "Veza nije pronađena")
    })
    public ResponseEntity<Void> ukloniDjeloSaSlucaja(@PathVariable Long slucajId, 
                                                       @PathVariable Long vezaId) {
        service.ukloniDjeloSaSlucaja(vezaId);
        return ResponseEntity.noContent().build();
    }
}