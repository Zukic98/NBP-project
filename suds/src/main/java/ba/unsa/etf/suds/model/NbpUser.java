package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date; // Koristimo java.sql.Date jer u ERD piše tip 'date'

/**
 * POJO koji reprezentira jedan red u tabeli nbp.NBP_USER — korisnički nalog
 * uposlenika koji se prijavljuje u SUDS sistem.
 *
 * <p>Veže se na: nbp.NBP_ROLE (kroz ROLE_ID), ADRESE (kroz ADDRESS_ID).
 * Koristi se kao FK u UPOSLENIK_PROFIL, TIM_NA_SLUCAJU, DOKAZI,
 * LANAC_NADZORA i FORENZICKI_IZVJESTAJI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpUser {
    /** Primarni ključ — kolona ID. */
    private Long id;

    /** Ime korisnika — kolona FIRST_NAME. */
    private String firstName;

    /** Prezime korisnika — kolona LAST_NAME. */
    private String lastName;

    /** E-mail adresa korisnika, mora biti jedinstven — kolona EMAIL. */
    private String email;

    /** Hashirana lozinka korisnika — kolona PASSWORD. */
    private String password;

    /** Korisničko ime za prijavu, mora biti jedinstven — kolona USERNAME. */
    private String username;

    /** Kontakt telefon korisnika — kolona PHONE_NUMBER. */
    private String phoneNumber;

    /** Datum rođenja korisnika — kolona BIRTH_DATE (tip DATE u Oracle). */
    private Date birthDate;

    /** FK na ADRESE.ADRESA_ID — adresa korisnika — kolona ADDRESS_ID. */
    private Long addressId;

    /** FK na nbp.NBP_ROLE.ID — uloga korisnika u sistemu — kolona ROLE_ID. */
    private Long roleId;
}