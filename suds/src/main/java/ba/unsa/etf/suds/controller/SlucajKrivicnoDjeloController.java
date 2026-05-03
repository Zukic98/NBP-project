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

/**
 * REST kontroler za upravljanje krivičnim djelima na konkretnom slučaju.
 *
 * <p>Bazna putanja: {@code /api/slucajevi/{slucajId}/krivicna-djela}. Pruža operacije
 * za dohvat, dodavanje (pojedinačno i grupno) i uklanjanje krivičnih djela sa slučaja.
 * Delegira sve operacije servisu {@code SlucajKrivicnoDjeloService}.
 */
@RestController
@RequestMapping("/api/slucajevi/{slucajId}/krivicna-djela")
@Tag(name = "Krivična djela na slučaju", description = "Upravljanje krivičnim djelima na konkretnom slučaju")
public class SlucajKrivicnoDjeloController {
    private final SlucajKrivicnoDjeloService service;

    /** Konstruktorska injekcija servisa za krivična djela na slučaju. */
    public SlucajKrivicnoDjeloController(SlucajKrivicnoDjeloService service) {
        this.service = service;
    }

    /**
     * GET /api/slucajevi/{slucajId}/krivicna-djela - dohvata sva krivična djela za slučaj.
     *
     * @param slucajId identifikator slučaja
     * @return 200 + lista {@link SlucajKrivicnoDjelo} za dati slučaj
     */
    @GetMapping
    @Operation(summary = "Dohvati sva krivična djela za slučaj")
    @ApiResponse(responseCode = "200", description = "Lista krivičnih djela vraćena")
    public ResponseEntity<List<SlucajKrivicnoDjelo>> getDjelaZaSlucaj(@PathVariable Long slucajId) {
        return ResponseEntity.ok(service.getDjelaZaSlucaj(slucajId));
    }

    /**
     * POST /api/slucajevi/{slucajId}/krivicna-djela - dodaje jedno krivično djelo na slučaj.
     *
     * @param slucajId identifikator slučaja
     * @param body     mapa sa ključem {@code djeloId} koji sadrži ID krivičnog djela
     * @return 201 + kreirani {@link SlucajKrivicnoDjelo},
     *         400 ako su podaci nevalidni,
     *         409 ako veza već postoji
     */
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

    /**
     * POST /api/slucajevi/{slucajId}/krivicna-djela/batch - dodaje više krivičnih djela na slučaj odjednom.
     *
     * @param slucajId identifikator slučaja
     * @param body     mapa sa ključem {@code djeloIds} koji sadrži listu ID-ova krivičnih djela
     * @return 201 bez sadržaja ako su sva krivična djela uspješno dodana,
     *         400 ako su podaci nevalidni
     */
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

    /**
     * DELETE /api/slucajevi/{slucajId}/krivicna-djela/{vezaId} - uklanja krivično djelo sa slučaja.
     *
     * @param slucajId identifikator slučaja
     * @param vezaId   identifikator veze između slučaja i krivičnog djela
     * @return 204 bez sadržaja,
     *         404 ako veza nije pronađena
     */
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