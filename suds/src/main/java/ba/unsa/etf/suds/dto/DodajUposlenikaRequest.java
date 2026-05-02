package ba.unsa.etf.suds.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Tijelo zahtjeva za POST /api/uposlenici.
 *
 * <p>Šef stanice koristi ovaj zahtjev za kreiranje novog uposlenika
 * unutar svoje stanice. Lozinka se hashira BCrypt-om na serveru.
 */
@Data
public class DodajUposlenikaRequest {
    /** Ime uposlenika. */
    private String firstName;

    /** Prezime uposlenika. */
    private String lastName;

    /** Email adresa uposlenika (koristi se i za prijavu). */
    private String email;

    /** Korisničko ime uposlenika. */
    private String username;

    /** Lozinka u plain-text obliku (BCrypt hashiranje radi server). */
    private String password;

    /** Kontakt telefon uposlenika. */
    private String phoneNumber;

    /** Datum rođenja uposlenika. */
    private LocalDate birthDate;

    /** ID adrese iz tabele ADRESE. */
    private Long addressId;

    /** ID uloge iz tabele NBP_ROLE. */
    private Long roleId;

    /** Broj značke uposlenika — jedinstven unutar stanice. */
    private String brojZnacke;
}