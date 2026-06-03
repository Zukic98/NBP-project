package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.PogledLanacNadzoraPregledDTO;
import ba.unsa.etf.suds.dto.PogledSlucajPregledDTO;
import ba.unsa.etf.suds.dto.PogledTimNaSlucajuPregledDTO;
import ba.unsa.etf.suds.service.PogledService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST kontroler za read-only Oracle poglede iz
 * {@code src/main/resources/db/pogledi.sql}.
 *
 * <p>Bazna putanja: {@code /api/pogledi}. Sve operacije su {@code GET} i
 * zahtijevaju autentifikaciju — bez dodatnog role-guarda jer su pogledi
 * sumarne, sigurne projekcije. Kreiranje DML-a kroz poglede nije moguće
 * (pogledi su {@code WITH READ ONLY}).
 */
@RestController
@RequestMapping("/api/pogledi")
@Tag(name = "Pogledi", description = "Read-only Oracle pogledi (POGLED_*)")
public class PogledController {

    private final PogledService pogledService;

    public PogledController(PogledService pogledService) {
        this.pogledService = pogledService;
    }

    @GetMapping("/slucajevi")
    @Operation(summary = "Pregled slučajeva sa stanicom, voditeljem i metrikama")
    @ApiResponse(responseCode = "200", description = "Lista pregleda slučajeva")
    public ResponseEntity<List<PogledSlucajPregledDTO>> getPreglediSlucajeva() {
        return ResponseEntity.ok(pogledService.getPreglediSlucajeva());
    }

    @GetMapping("/lanac-nadzora")
    @Operation(summary = "Pregled primopredaja dokaza sa imenima učesnika")
    @ApiResponse(responseCode = "200", description = "Lista pregleda lanca nadzora")
    public ResponseEntity<List<PogledLanacNadzoraPregledDTO>> getPreglediLancaNadzora() {
        return ResponseEntity.ok(pogledService.getPreglediLancaNadzora());
    }

    @GetMapping("/tim-na-slucaju")
    @Operation(summary = "Pregled članova tima po slučaju sa sistemskim ulogama")
    @ApiResponse(responseCode = "200", description = "Lista pregleda tima na slučaju")
    public ResponseEntity<List<PogledTimNaSlucajuPregledDTO>> getPreglediTimaNaSlucaju() {
        return ResponseEntity.ok(pogledService.getPreglediTimaNaSlucaju());
    }
}
