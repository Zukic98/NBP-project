package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DokazListDTO;
import ba.unsa.etf.suds.dto.DokazStanjeDTO;
import ba.unsa.etf.suds.dto.KreirajDokazRequest;
import ba.unsa.etf.suds.dto.LanacDetaljiDTO;
import ba.unsa.etf.suds.dto.UpdateStatusRequest;
import ba.unsa.etf.suds.service.DokazService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Dokazi", description = "Upravljanje dokazima")
public class DokazController {

    private final DokazService dokazService;

    public DokazController(DokazService dokazService) {
        this.dokazService = dokazService;
    }

    @GetMapping("/slucajevi/{caseId}/dokazi")
    @Operation(summary = "Dohvati dokaze za slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dokazi uspješno dohvaćeni"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<List<DokazListDTO>> getDokaziBySlucaj(@PathVariable("caseId") Long caseId) {
        try {
            return ResponseEntity.ok(dokazService.getBySlucajId(caseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/slucajevi/{caseId}/dokazi")
    @Operation(summary = "Kreiraj novi dokaz za slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dokaz kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<DokazListDTO> kreirajDokazZaSlucaj(
            @PathVariable("caseId") Long caseId,
            @RequestBody KreirajDokazRequest request) {
        try {
            String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userIdStr);

            DokazListDTO kreiran = dokazService.kreirajZaSlucaj(caseId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(kreiran);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/dokazi/{id}/stanje")
    @Operation(summary = "Dohvati stanje dokaza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stanje dokaza dohvaćeno"),
            @ApiResponse(responseCode = "404", description = "Dokaz nije pronađen"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<DokazStanjeDTO> getDokazStanje(@PathVariable("id") Long dokazId) {
        try {
            String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userIdStr);

            DokazStanjeDTO stanje = dokazService.getStanje(dokazId, userId);
            return ResponseEntity.ok(stanje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/dokazi/{id}/lanac")
    @Operation(summary = "Dohvati lanac nadzora za dokaz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lanac nadzora dohvaćen"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<Map<String, List<LanacDetaljiDTO>>> getLanac(@PathVariable("id") Long dokazId) {
        try {
            List<LanacDetaljiDTO> lanacList = dokazService.getLanacWithNames(dokazId);
            return ResponseEntity.ok(Map.of("lanac", lanacList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/dokazi/{id}/status")
    @Operation(summary = "Ažuriraj status dokaza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status dokaza ažuriran"),
            @ApiResponse(responseCode = "404", description = "Dokaz nije pronađen"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<Map<String, String>> azurirajStatus(
            @PathVariable("id") Long dokazId,
            @RequestBody UpdateStatusRequest request) {
        try {
            dokazService.azurirajStatus(dokazId, request.getStatus());
            return ResponseEntity.ok(Map.of("message", "Status ažuriran"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
