package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.security.JwtUtil;
import ba.unsa.etf.suds.service.UposlenikService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uposlenici")
@Tag(name = "Uposlenici", description = "Upravljanje uposlenicima")
public class UposlenikController {

    private final UposlenikService uposlenikService;
    private final JwtUtil jwtUtil;

    public UposlenikController(UposlenikService uposlenikService, JwtUtil jwtUtil) {
        this.uposlenikService = uposlenikService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @Operation(summary = "Dohvati sve uposlenike stanice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista uposlenika vraćena"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<List<UposlenikDTO>> getAll(HttpServletRequest httpRequest) {
        try {
            String token = extractToken(httpRequest);
            Long stanicaId = jwtUtil.extractStanicaId(token);
            
            List<UposlenikDTO> uposlenici = uposlenikService.getUposleniciPoStanici(stanicaId);
            
            return ResponseEntity.ok(uposlenici);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvati uposlenika po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uposlenik pronađen"),
            @ApiResponse(responseCode = "404", description = "Uposlenik nije pronađen")
    })
    public ResponseEntity<UposlenikDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(uposlenikService.getUposlenikById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ažuriraj podatke uposlenika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Podaci uposlenika ažurirani"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup")
    })
    public ResponseEntity<?> azurirajPodatke(
            @PathVariable Long id,
            @RequestBody UposlenikDTO request,
            HttpServletRequest httpRequest) {
        try {
            String token = extractToken(httpRequest);
            Long stanicaId = jwtUtil.extractStanicaId(token);

            uposlenikService.azurirajPodatke(id, request, stanicaId);

            return ResponseEntity.ok("Podaci uposlenika uspješno ažurirani!");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Greška: " + e.getMessage());
        }
    }
    @PutMapping("/{id}/password-reset")
    @Operation(summary = "Resetuj lozinku uposlenika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lozinka resetovana"),
            @ApiResponse(responseCode = "404", description = "Uposlenik nije pronađen")
    })
public ResponseEntity<?> resetLozinke(@PathVariable Long id, @RequestBody java.util.Map<String, String> request, HttpServletRequest httpRequest) {
    String token = extractToken(httpRequest);
    Long stanicaId = jwtUtil.extractStanicaId(token);
    uposlenikService.resetujLozinku(id, request.get("novaLozinka"), stanicaId);
    return ResponseEntity.ok("Lozinka uspješno resetovana.");
}

   @PostMapping
    @Operation(summary = "Dodaj novog uposlenika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Uposlenik dodan"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<?> dodajUposlenika(
            @RequestBody DodajUposlenikaRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractToken(httpRequest);
            Long stanicaId = jwtUtil.extractStanicaId(token);
            
            uposlenikService.dodajUposlenika(request, stanicaId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body("Uposlenik uspješno dodat!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Promijeni status uposlenika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status uposlenika promijenjen"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<?> promijeniStatus(
            @PathVariable Long id,
            @RequestBody PromijeniStatusRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractToken(httpRequest);
            Long stanicaId = jwtUtil.extractStanicaId(token);
            String currentUserIdStr = jwtUtil.extractUserId(token);
            Long currentUserId = Long.parseLong(currentUserIdStr);
            
            uposlenikService.promijeniStatus(id, request, stanicaId, currentUserId);
            
            return ResponseEntity.ok("Status uspješno promijenjen u: " + request.getStatus());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Došlo je do neočekivane greške: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Authorization header nije pronađen!");
    }
}
