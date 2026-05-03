package ba.unsa.etf.suds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Interni DTO koji AuthService koristi za dohvat korisničkih podataka pri prijavi.
 *
 * <p>Nije direktno izložen kao HTTP odgovor — koristi se interno između
 * repozitorija i servisa za autentifikaciju. Sadrži hashiranu lozinku
 * potrebnu za BCrypt provjeru.
 */
@Data
@AllArgsConstructor
public class UposlenikLoginDTO {
    /** Primarni ključ korisnika iz tabele NBP_USER. */
    private Long userId;

    /** Ime uposlenika. */
    private String ime;

    /** Prezime uposlenika. */
    private String prezime;

    /** Email adresa uposlenika. */
    private String email;

    /** BCrypt-hashirana lozinka uposlenika. */
    private String password;

    /** Naziv sistemske uloge uposlenika (npr. "INSPEKTOR"). */
    private String uloga;

    /** Broj značke uposlenika. */
    private String brojZnacke;

    /** Radni status uposlenika. */
    private String status;

    /** ID policijske stanice kojoj uposlenik pripada. */
    private Long stanicaId;
}