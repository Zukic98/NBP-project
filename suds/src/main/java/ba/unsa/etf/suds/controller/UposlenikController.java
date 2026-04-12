package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.security.JwtUtil;
import ba.unsa.etf.suds.service.UposlenikService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uposlenici")
public class UposlenikController {

    private final UposlenikService uposlenikService;
    private final JwtUtil jwtUtil;

    public UposlenikController(UposlenikService uposlenikService, JwtUtil jwtUtil) {
        this.uposlenikService = uposlenikService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
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
    public ResponseEntity<UposlenikDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(uposlenikService.getUposlenikById(id));
    }

    @PutMapping("/{id}")
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
public ResponseEntity<?> resetLozinke(@PathVariable Long id, @RequestBody java.util.Map<String, String> request, HttpServletRequest httpRequest) {
    String token = extractToken(httpRequest);
    Long stanicaId = jwtUtil.extractStanicaId(token);
    uposlenikService.resetujLozinku(id, request.get("novaLozinka"), stanicaId);
    return ResponseEntity.ok("Lozinka uspješno resetovana.");
}

   @PostMapping
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