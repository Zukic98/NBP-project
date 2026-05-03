package ba.unsa.etf.suds.dto;

import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/stanice/register.
 *
 * <p>Registrira novu policijsku stanicu zajedno s prvim korisnikom
 * (šefom stanice). Adresa stanice može se proslijediti kao slobodna
 * polja ili kao referenca na postojeću adresu putem {@code adresaId}.
 */
@Data
public class RegistrationRequest {
    /** Naziv policijske stanice koja se registrira. */
    private String imeStanice;

    /** Ulica i kućni broj sjedišta stanice. */
    private String ulicaIBroj;

    /** Grad u kojem se stanica nalazi. */
    private String grad;

    /** Poštanski broj sjedišta stanice. */
    private String postanskiBroj;

    /** Ime šefa stanice (prvog korisnika). */
    private String firstName;

    /** Prezime šefa stanice (prvog korisnika). */
    private String lastName;

    /** Email adresa šefa stanice. */
    private String email;

    /** Lozinka šefa stanice u plain-text obliku. */
    private String password;

    /** Korisničko ime šefa stanice. */
    private String username;

    /** Broj značke šefa stanice. */
    private String brojZnacke;

    /** ID postojeće adrese iz tabele ADRESE (alternativa slobodnim poljima). */
    private Long adresaId;
}