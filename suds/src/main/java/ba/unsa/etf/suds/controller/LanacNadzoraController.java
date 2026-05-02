package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.dto.PotvrdaRequest;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.service.LanacNadzoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje lancem nadzora dokaza.
 *
 * <p>Bazna putanja: {@code /api/lanac-nadzora}. Pruža operacije za slanje dokaza,
 * pregled vlastitih zahtjeva, prihvatanje i potvrdu/odbijanje primopredaje.
 * Identitet korisnika se čita iz {@code SecurityContextHolder} (principal = userId kao String).
 * Delegira sve operacije servisu {@code LanacNadzoraService}.
 */
@RestController
@RequestMapping("/api/lanac-nadzora")
@Tag(name = "Lanac nadzora", description = "Lanac nadzora dokaza")
public class LanacNadzoraController {
    private final LanacNadzoraService service;

    /** Konstruktorska injekcija servisa za lanac nadzora. */
    public LanacNadzoraController(LanacNadzoraService service) {
        this.service = service;
    }

    /**
     * POST /api/lanac-nadzora/posalji - šalje dokaz na primopredaju.
     *
     * <p>Identitet pošiljaoca se čita iz {@code SecurityContextHolder} (principal = userId).
     *
     * @param request tijelo zahtjeva sa podacima o slanju dokaza
     * @return 200 + kreirani {@link LanacNadzora} unos,
     *         400 ako je zahtjev neispravan,
     *         500 pri grešci na serveru
     */
    @PostMapping("/posalji")
    @Operation(summary = "Pošalji dokaz na primopredaju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zahtjev za slanje kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<LanacNadzora> posaljiDokaz(
            @RequestBody PosaljiDokazRequest request) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        LanacNadzora lanac = service.posaljiDokaz(request, userId);
        return ResponseEntity.ok(lanac);
    }

    /**
     * GET /api/lanac-nadzora/moji-zahtjevi - dohvata zahtjeve u lancu nadzora za prijavljenog korisnika.
     *
     * <p>Identitet korisnika se čita iz {@code SecurityContextHolder} (principal = userId).
     *
     * @return 200 + lista {@link LanacNadzora} zahtjeva vezanih za prijavljenog korisnika
     */
    @GetMapping("/moji-zahtjevi")
    @Operation(summary = "Dohvati moje zahtjeve u lancu nadzora")
    @ApiResponse(responseCode = "200", description = "Lista zahtjeva vraćena")
    public ResponseEntity<List<LanacNadzora>> getMojiZahtjevi() {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        List<LanacNadzora> zahtjevi = service.getMojiZahtjevi(userId);
        return ResponseEntity.ok(zahtjevi);
    }

    /**
     * PUT /api/lanac-nadzora/prihvati/{id} - prihvata zaprimljeni dokaz.
     *
     * <p>Identitet primaoca se čita iz {@code SecurityContextHolder} (principal = userId).
     *
     * @param unosId identifikator unosa u lancu nadzora
     * @return 200 bez sadržaja ako je prihvatanje uspješno,
     *         404 ako unos nije pronađen
     */
    @PutMapping("/prihvati/{id}")
    @Operation(summary = "Prihvati zaprimljeni dokaz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dokaz prihvaćen"),
            @ApiResponse(responseCode = "404", description = "Unos nije pronađen")
    })
    public ResponseEntity<Void> prihvatiDokaz(@PathVariable("id") Long unosId) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        service.prihvatiDokaz(unosId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH /api/lanac-nadzora/{unosId}/potvrda - potvrđuje ili odbija primopredaju.
     *
     * <p>Identitet korisnika se čita iz {@code SecurityContextHolder} (principal = userId).
     *
     * @param unosId  identifikator unosa u lancu nadzora
     * @param request tijelo zahtjeva sa statusom potvrde i opcionalnom napomenom
     * @return 200 bez sadržaja ako je status ažuriran,
     *         404 ako unos nije pronađen
     */
    @PatchMapping("/{unosId}/potvrda")
    @Operation(summary = "Potvrdi ili odbij primopredaju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status potvrde ažuriran"),
            @ApiResponse(responseCode = "404", description = "Unos nije pronađen")
    })
    public ResponseEntity<Void> potvrdiIliOdbij(@PathVariable Long unosId,
                                                @RequestBody PotvrdaRequest request) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        service.potvrdiIliOdbij(unosId, request.getStatus(), request.getNapomena(), userId);
        return ResponseEntity.ok().build();
    }
}
