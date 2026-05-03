package ba.unsa.etf.suds.dto;

import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/auth/login.
 *
 * <p>Identitet se utvrđuje kombinacijom (email, brojZnacke), a lozinka se
 * provjerava preko BCrypt-a. Pogledati AuthService#login.
 */
@Data
public class LoginRequest {
    /** Email adresa uposlenika. */
    private String email;

    /** Lozinka u plain-text obliku (BCrypt provjera radi server). */
    private String password;

    /** Broj značke — razlikuje uposlenike s istim email-om u različitim stanicama. */
    private String brojZnacke;
}
