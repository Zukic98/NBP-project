package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.service.AuthService;
import ba.unsa.etf.suds.service.UposlenikService; 
import ba.unsa.etf.suds.security.JwtUtil; 
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok("Uspješno ste se odjavili.");
    }

    @PutMapping("/promijeni-lozinku")
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