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

/**
 * REST kontroler za upravljanje uposlenicima policijske stanice.
 *
 * <p>Izlaže resurse na putanji {@code /api/uposlenici} i delegira poslovnu logiku
 * servisu {@link UposlenikService}. Većina handlera ekstrahuje {@code stanicaId}
 * iz JWT tokena putem {@link JwtUtil#extractStanicaId(String)} kako bi operacije
 * bile ograničene na stanicu trenutno prijavljenog korisnika.</p>
 */
@RestController
@RequestMapping("/api/uposlenici")
@Tag(name = "Uposlenici", description = "Upravljanje uposlenicima")
public class UposlenikController {

    private final UposlenikService uposlenikService;
    private final JwtUtil jwtUtil;

    /**
     * Kreira instancu kontrolera s injektovanim servisom i JWT pomoćnom klasom.
     *
     * @param uposlenikService servis za upravljanje uposlenicima
     * @param jwtUtil          pomoćna klasa za ekstrakciju podataka iz JWT tokena
     */
    public UposlenikController(UposlenikService uposlenikService, JwtUtil jwtUtil) {
        this.uposlenikService = uposlenikService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * {@code GET /api/uposlenici} — vraća listu svih uposlenika stanice
     * trenutno prijavljenog korisnika.
     *
     * <p>Identifikator stanice ekstrahuje se iz JWT tokena pozivom
     * {@link JwtUtil#extractStanicaId(String)}.</p>
     *
     * @param httpRequest HTTP zahtjev iz kojeg se čita {@code Authorization} zaglavlje
     * @return HTTP 200 s listom uposlenika, ili HTTP 500 u slučaju greške
     */
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

    /**
     * {@code GET /api/uposlenici/{id}} — vraća podatke uposlenika s traženim ID-om.
     *
     * @param id jedinstveni identifikator uposlenika
     * @return HTTP 200 s podacima uposlenika, ili HTTP 404 ako uposlenik nije pronađen
     */
    @GetMapping("/{id}")
    @Operation(summary = "Dohvati uposlenika po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uposlenik pronađen"),
            @ApiResponse(responseCode = "404", description = "Uposlenik nije pronađen")
    })
    public ResponseEntity<UposlenikDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(uposlenikService.getUposlenikById(id));
    }

    /**
     * {@code PUT /api/uposlenici/{id}} — ažurira lične i profesionalne podatke
     * uposlenika s traženim ID-om.
     *
     * <p>Identifikator stanice ekstrahuje se iz JWT tokena pozivom
     * {@link JwtUtil#extractStanicaId(String)} kako bi se spriječilo
     * mijenjanje uposlenika iz druge stanice.</p>
     *
     * @param id          jedinstveni identifikator uposlenika
     * @param request     novi podaci uposlenika
     * @param httpRequest HTTP zahtjev iz kojeg se čita {@code Authorization} zaglavlje
     * @return HTTP 200 s porukom uspjeha, HTTP 403 ako uposlenik nije u istoj stanici,
     *         ili HTTP 400 u slučaju druge greške
     */
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
    /**
     * {@code PUT /api/uposlenici/{id}/password-reset} — resetuje lozinku uposlenika
     * s traženim ID-om.
     *
     * <p>Identifikator stanice ekstrahuje se iz JWT tokena pozivom
     * {@link JwtUtil#extractStanicaId(String)} kako bi se osiguralo da se
     * resetuje lozinka uposlenika iz iste stanice.</p>
     *
     * @param id          jedinstveni identifikator uposlenika
     * @param request     mapa s ključem {@code novaLozinka} koji sadrži novu lozinku
     * @param httpRequest HTTP zahtjev iz kojeg se čita {@code Authorization} zaglavlje
     * @return HTTP 200 s porukom uspjeha, ili HTTP 404 ako uposlenik nije pronađen
     */
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

    /**
     * {@code POST /api/uposlenici} — dodaje novog uposlenika u stanicu
     * trenutno prijavljenog korisnika.
     *
     * <p>Identifikator stanice ekstrahuje se iz JWT tokena pozivom
     * {@link JwtUtil#extractStanicaId(String)}.</p>
     *
     * @param request     podaci novog uposlenika
     * @param httpRequest HTTP zahtjev iz kojeg se čita {@code Authorization} zaglavlje
     * @return HTTP 201 s porukom uspjeha, HTTP 400 u slučaju poslovne greške,
     *         ili HTTP 500 u slučaju neočekivane greške
     */
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

    /**
     * {@code PUT /api/uposlenici/{id}/status} — mijenja status uposlenika
     * (npr. aktivan/neaktivan).
     *
     * <p>Identifikator stanice i ID trenutnog korisnika ekstrahuju se iz JWT tokena
     * pozivima {@link JwtUtil#extractStanicaId(String)} i
     * {@link JwtUtil#extractUserId(String)}.</p>
     *
     * @param id          jedinstveni identifikator uposlenika čiji se status mijenja
     * @param request     novi status uposlenika
     * @param httpRequest HTTP zahtjev iz kojeg se čita {@code Authorization} zaglavlje
     * @return HTTP 200 s porukom uspjeha, HTTP 403 ako je pristup zabranjen,
     *         HTTP 400 u slučaju poslovne greške, ili HTTP 500 u slučaju neočekivane greške
     */
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

    /**
     * Ekstrahuje JWT token iz {@code Authorization: Bearer <token>} zaglavlja zahtjeva.
     *
     * @param request HTTP zahtjev
     * @return JWT token bez prefiksa {@code Bearer }
     * @throws RuntimeException ako zaglavlje nije prisutno ili nema ispravan format
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Authorization header nije pronađen!");
    }
}
