package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.dto.UposlenikLoginDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import ba.unsa.etf.suds.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servis za autentifikaciju i upravljanje JWT tokenima.
 *
 * <p>Implementira login (email + broj značke + lozinka), logout (dodavanje
 * tokena u {@code CRNA_LISTA_TOKENA}) i izradu JWT-a kroz {@link JwtUtil}.
 * Lozinke se provjeravaju kroz {@link BCryptPasswordEncoder}.
 * Orkestrira {@link UposlenikRepository}.
 */
@Service
public class AuthService {

    private final UposlenikRepository uposlenikRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** Konstruktorska injekcija svih ovisnosti. */
    public AuthService(UposlenikRepository uposlenikRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.uposlenikRepository = uposlenikRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Prijavljuje uposlenika i vraća potpisani JWT token.
     *
     * <p>Identitet se utvrđuje kombinacijom (email, brojZnacke); lozinka se
     * provjerava BCrypt-om. Ako je {@code UPOSLENIK_PROFIL.STATUS} postavljen
     * na {@code 'Penzionisan'} ili {@code 'Otpušten'}, baca se
     * {@link RuntimeException} s porukom "Pristup odbijen".
     *
     * @param request DTO sa email-om, lozinkom i brojem značke
     * @return DTO sa potpisanim JWT tokenom (validan po default-u 24h)
     * @throws RuntimeException ako korisnik nije pronađen, lozinka nije ispravna
     *                          ili je nalog deaktiviran (Penzionisan/Otpušten)
     */
    public LoginResponse login(LoginRequest request) {
    UposlenikLoginDTO uposlenik = uposlenikRepository.findByEmailAndZnacka(request.getEmail(), request.getBrojZnacke())
            .orElseThrow(() -> new RuntimeException("Nevalidni podaci: Korisnik nije pronađen!"));

    if (!passwordEncoder.matches(request.getPassword(), uposlenik.getPassword())) {
        throw new RuntimeException("Nevalidni podaci: Pogrešna lozinka!");
    }

    if ("Penzionisan".equalsIgnoreCase(uposlenik.getStatus()) || "Otpušten".equalsIgnoreCase(uposlenik.getStatus())) {
        throw new RuntimeException("Pristup odbijen: Vaš nalog je deaktiviran (" + uposlenik.getStatus() + ")");
    }

    String token = jwtUtil.generateToken(
            uposlenik.getUserId(), 
            uposlenik.getUloga(), 
            uposlenik.getStanicaId()
    );

    return new LoginResponse(token, "Bearer");
}

    /**
     * Odjavljuje uposlenika dodavanjem JWT tokena u crnu listu.
     *
     * <p>Ako zaglavlje {@code Authorization} sadrži prefiks {@code Bearer },
     * token se ekstrahuje i upisuje u tabelu {@code CRNA_LISTA_TOKENA} putem
     * {@link UposlenikRepository#dodajUTabeluCrnaLista(String)}.
     * Naredni zahtjevi s tim tokenom bit će odbijeni od strane JWT filtera.
     *
     * @param authHeader vrijednost HTTP zaglavlja {@code Authorization}
     *                   (npr. {@code "Bearer eyJhbGci..."})
     */
    public void logout(String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        uposlenikRepository.dodajUTabeluCrnaLista(token);
    }
}
}