package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DodajOsumnjicenogRequest;
import ba.unsa.etf.suds.dto.OsumnjiceniDTO;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate; 

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje osumnjičenima.
 *
 * <p>Bazna putanja: {@code /api}. Pruža operacije za dohvat svih osumnjičenih,
 * dohvat osumnjičenih po slučaju i dodavanje novog osumnjičenog na slučaj.
 * Kreiranje osumnjičenog zahtijeva ulogu {@code SEF_STANICE} ili {@code INSPEKTOR}.
 * Delegira sve operacije servisu {@code OsumnjiceniService}.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Osumnjičeni", description = "Upravljanje osumnjičenima")
public class OsumnjiceniController {

    private final OsumnjiceniService service;

    /** Konstruktorska injekcija servisa za osumnjičene. */
    public OsumnjiceniController(OsumnjiceniService service) {
        this.service = service;
    }

    /**
     * GET /api/osumnjiceni - dohvata sve osumnjičene u sistemu.
     *
     * @return 200 + lista svih {@link Osumnjiceni}
     */
    @GetMapping("/osumnjiceni")
    @Operation(summary = "Dohvati sve osumnjičene u sistemu")
    @ApiResponse(responseCode = "200", description = "Lista svih osumnjičenih vraćena")
    public ResponseEntity<List<Osumnjiceni>> getAll() {
        return ResponseEntity.ok(service.getAllOsumnjiceni());
    }


    /**
     * GET /api/slucajevi/{caseId}/osumnjiceni - dohvata osumnjičene za određeni slučaj.
     *
     * @param caseId identifikator slučaja
     * @return 200 + lista {@link OsumnjiceniDTO} za dati slučaj
     */
    @GetMapping("/slucajevi/{caseId}/osumnjiceni")
    @Operation(summary = "Dohvati osumnjičene za konkretan slučaj")
    public ResponseEntity<List<OsumnjiceniDTO>> getByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(service.getOsumnjiceniBySlucajId(caseId));
    }


    /**
     * POST /api/slucajevi/{caseId}/osumnjiceni - dodaje novog osumnjičenog na slučaj.
     *
     * <p>Samo {@code SEF_STANICE} i {@code INSPEKTOR} mogu dodavati osumnjičene
     * ({@code @PreAuthorize("hasAnyRole('SEF_STANICE', 'INSPEKTOR')")}).
     * Datum rođenja ne smije biti u budućnosti.
     *
     * @param caseId  identifikator slučaja
     * @param request tijelo zahtjeva sa podacima novog osumnjičenog
     * @return 201 + kreirani {@link Osumnjiceni},
     *         400 ako je datum rođenja u budućnosti,
     *         403 ako korisnik nema odgovarajuću ulogu,
     *         500 pri grešci na serveru
     */
    @PostMapping("/slucajevi/{caseId}/osumnjiceni")
    @PreAuthorize("hasAnyRole('SEF_STANICE', 'INSPEKTOR')")
    public ResponseEntity<?> create(
            @PathVariable Long caseId, 
            @RequestBody DodajOsumnjicenogRequest request) {
        
        try {
            if (request.getDatumRodjenja() != null) {
                if (request.getDatumRodjenja().toLocalDate().isAfter(LocalDate.now())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Greška: Datum rođenja ne može biti u budućnosti.");
                }
            }

            Osumnjiceni novi = service.dodajOsumnjicenog(caseId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(novi);
            
        } catch (Exception e) { 
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greška na serveru: " + e.getMessage());
        }
    }
}