package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.dto.UposlenikLoginDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import ba.unsa.etf.suds.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UposlenikRepository uposlenikRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UposlenikRepository uposlenikRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.uposlenikRepository = uposlenikRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        UposlenikLoginDTO uposlenik = uposlenikRepository.findByEmailAndZnacka(request.getEmail(), request.getBrojZnacke())
                .orElseThrow(() -> new RuntimeException("Nevalidni podaci: Korisnik nije pronađen!"));

        if ("Penzionisan".equalsIgnoreCase(uposlenik.getStatus()) || "Otpušten".equalsIgnoreCase(uposlenik.getStatus())) {
            throw new RuntimeException("Pristup odbijen: Vaš nalog je deaktiviran (Status: " + uposlenik.getStatus() + ")");
        }

        if (!passwordEncoder.matches(request.getPassword(), uposlenik.getPassword())) {
            throw new RuntimeException("Nevalidni podaci: Pogrešna lozinka!");
        }

        String token = jwtUtil.generateToken(
                uposlenik.getUserId(), 
                uposlenik.getUloga(), 
                uposlenik.getStanicaId()
        );

        return new LoginResponse(token, "Bearer");
    }

    public void logout(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        uposlenikRepository.dodajUTabeluCrnaLista(token);
    }
}
}