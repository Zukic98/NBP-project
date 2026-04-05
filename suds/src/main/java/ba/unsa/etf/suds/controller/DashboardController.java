package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import ba.unsa.etf.suds.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UposlenikRepository uposlenikRepository;
    private final JwtUtil jwtUtil;

    public DashboardController(UposlenikRepository uposlenikRepository, JwtUtil jwtUtil) {
        this.uposlenikRepository = uposlenikRepository;
        this.jwtUtil = jwtUtil;
    }

@GetMapping("/me")
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