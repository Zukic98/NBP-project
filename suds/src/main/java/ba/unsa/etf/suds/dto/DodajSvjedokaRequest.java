package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/slucajevi/{id}/svjedoci (dodavanje postojećeg svjedoka).
 *
 * <p>Adresa se može proslijediti kao jednopoljna vrijednost ({@code adresa})
 * ili kao strukturirana polja (ulicaIBroj, grad, postanskiBroj, drzava).
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DodajSvjedokaRequest {
    /** Puno ime i prezime svjedoka. */
    private String imePrezime;

    /** Jedinstveni matični broj građana svjedoka. */
    private String jmbg;

    /** Kontakt telefon svjedoka. */
    private String kontaktTelefon;

    /** Dodatne bilješke o svjedoku ili iskazu. */
    private String biljeska;

    /** Jednopoljna adresa iz frontenda (koristi se ako ulicaIBroj nije postavljen) */
    private String adresa;

    /** Ulica i kućni broj adrese stanovanja svjedoka. */
    private String ulicaIBroj;

    /** Grad adrese stanovanja svjedoka. */
    private String grad;

    /** Poštanski broj adrese stanovanja svjedoka. */
    private String postanskiBroj;

    /** Država adrese stanovanja svjedoka. */
    private String drzava;
}
