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
    public ResponseEntity<List<UposlenikDTO>> getAll() {
        return ResponseEntity.ok(uposlenikService.getAllUposlenici());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UposlenikDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(uposlenikService.getUposlenikById(id));
    }

    @PostMapping
    public ResponseEntity<?> dodajUposlenika(
            @RequestBody DodajUposlenikaRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractToken(httpRequest);
        Long stanicaId = jwtUtil.extractStanicaId(token);
        
        uposlenikService.dodajUposlenika(request, stanicaId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Uposlenik uspješno dodat!");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> promijeniStatus(
            @PathVariable Long id,
            @RequestBody PromijeniStatusRequest request,
            HttpServletRequest httpRequest) {
        
        String token = extractToken(httpRequest);
        Long stanicaId = jwtUtil.extractStanicaId(token);
        String currentUserIdStr = jwtUtil.extractUserId(token);
        Long currentUserId = Long.parseLong(currentUserIdStr);
        
        uposlenikService.promijeniStatus(id, request, stanicaId, currentUserId);
        
        return ResponseEntity.ok("Status promijenjen u: " + request.getStatus());
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Authorization header nije pronađen!");
    }
}