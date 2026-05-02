package ba.unsa.etf.suds.dto;

import lombok.Data;

import java.sql.Date;

/**
 * Tijelo zahtjeva za POST /api/slucajevi/{id}/osumnjiceni.
 *
 * <p>Kreira novog osumnjičenog i vezuje ga za slučaj. Adresa se
 * proslijeđuje kao slobodna polja koja se upisuju u tabelu ADRESE.
 */
@Data
public class DodajOsumnjicenogRequest {
    /** Puno ime i prezime osumnjičenog. */
    private String imePrezime;

    /** Jedinstveni matični broj građana osumnjičenog. */
    private String jmbg;

    /** Datum rođenja osumnjičenog. */
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private Date datumRodjenja;

    /** Ulica i kućni broj adrese stanovanja osumnjičenog. */
    private String ulicaIBroj;

    /** Grad adrese stanovanja osumnjičenog. */
    private String grad;

    /** Poštanski broj adrese stanovanja osumnjičenog. */
    private String postanskiBroj;

    /** Država adrese stanovanja osumnjičenog. */
    private String drzava;
}
