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

/**
 * REST kontroler za autentifikaciju (prijava, odjava, vlastita lozinka).
 *
 * <p>Bazna putanja: {@code /api/auth}. Endpoint za prijavu ({@code POST /login})
 * je javan; ostali zahtijevaju validan JWT token u {@code Authorization} headeru.
 * Delegira logiku autentifikacije servisu {@code AuthService}, a upravljanje
 * lozinkama servisu {@code UposlenikService}.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autentifikacija", description = "Prijava, odjava i upravljanje lozinkama")
public class AuthController {

    private final AuthService authService;
    private final UposlenikService uposlenikService;
    private final JwtUtil jwtUtil;

    /** Konstruktorska injekcija servisa za autentifikaciju, uposlenike i JWT pomoćnih metoda. */
    public AuthController(AuthService authService, UposlenikService uposlenikService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.uposlenikService = uposlenikService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/auth/login - prijava korisnika.
     *
     * <p>Javan endpoint (ne zahtijeva token). Validira kombinaciju email/brojZnacke/lozinka
     * i u slučaju uspjeha vraća potpisani JWT token.
     *
     * @param loginRequest tijelo zahtjeva sa kredencijalima korisnika
     * @return 200 + {@link LoginResponse} sa JWT tokenom ako je prijava uspješna,
     *         401 ako su kredencijali neispravni,
     *         403 ako je korisnički račun deaktiviran
     */
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

    /**
     * POST /api/auth/logout - odjava trenutnog korisnika i stavljanje tokena na crnu listu.
     *
     * @param token Authorization header u obliku {@code "Bearer <jwt>"}
     * @return 200 sa potvrdnom porukom o uspješnoj odjavi
     */
    @PostMapping("/logout")
    @Operation(summary = "Odjava korisnika")
    @ApiResponse(responseCode = "200", description = "Uspješna odjava")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok("Uspješno ste se odjavili.");
    }

    /**
     * PUT /api/auth/promijeni-lozinku - promjena vlastite lozinke prijavljenog korisnika.
     *
     * <p>Identitet korisnika se utvrđuje iz JWT tokena putem {@code JwtUtil.extractUserId}.
     * Tijelo zahtjeva mora sadržavati polja {@code staraLozinka} i {@code novaLozinka}.
     *
     * @param request     mapa sa ključevima {@code staraLozinka} i {@code novaLozinka}
     * @param httpRequest HTTP zahtjev iz kojeg se izvlači JWT token
     * @return 200 sa potvrdnom porukom, 400 ako stara lozinka nije ispravna ili su podaci nevalidni
     */
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

    /** Pomoćna metoda — izvlači čist JWT iz {@code Authorization} headera (uklanja prefiks "Bearer "). */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token nije pronađen u headeru!");
    }
}
