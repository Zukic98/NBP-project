package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.service.AuthService;
import ba.unsa.etf.suds.service.UposlenikService;
import ba.unsa.etf.suds.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autentifikacija", description = "Prijava, odjava i upravljanje lozinkama")
public class AuthController {

    private final AuthService authService;
    private final UposlenikService uposlenikService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UposlenikService uposlenikService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.uposlenikService = uposlenikService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "Prijava korisnika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uspješna prijava"),
            @ApiResponse(responseCode = "401", description = "Neispravni kredencijali"),
            @ApiResponse(responseCode = "403", description = "Pristup odbijen")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Pristup odbijen")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Odjava korisnika")
    @ApiResponse(responseCode = "200", description = "Uspješna odjava")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok("Uspješno ste se odjavili.");
    }

    @PutMapping("/promijeni-lozinku")
    @Operation(summary = "Promijeni vlastitu lozinku")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lozinka promijenjena"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev")
    })
    public ResponseEntity<?> promijeniMojuLozinku(@RequestBody java.util.Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String token = extractToken(httpRequest);
            Long userId = Long.parseLong(jwtUtil.extractUserId(token));
            uposlenikService.promijeniLicnuLozinku(userId, request.get("staraLozinka"), request.get("novaLozinka"));
            return ResponseEntity.ok("Lozinka uspješno promijenjena.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token nije pronađen u headeru!");
    }
}
