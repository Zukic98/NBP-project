package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import ba.unsa.etf.suds.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Korisničke informacije")
public class DashboardController {

    private final UposlenikRepository uposlenikRepository;
    private final JwtUtil jwtUtil;

    public DashboardController(UposlenikRepository uposlenikRepository, JwtUtil jwtUtil) {
        this.uposlenikRepository = uposlenikRepository;
        this.jwtUtil = jwtUtil;
    }

@GetMapping("/me")
@Operation(summary = "Dohvati profil prijavljenog korisnika")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profil korisnika vraćen"),
        @ApiResponse(responseCode = "401", description = "Neautorizovan pristup")
})
public ResponseEntity<?> getMyProfile() {
    try {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        
        Long userId = Long.parseLong(userIdStr);

        UposlenikDTO profil = uposlenikRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        return ResponseEntity.ok(profil);
    } catch (Exception e) {
        return ResponseEntity.status(401).body("Neautorizovan pristup");
    }
}
}
