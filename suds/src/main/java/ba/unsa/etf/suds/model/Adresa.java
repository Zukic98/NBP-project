package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u tabeli ADRESE — fizička adresa koja se
 * koristi kao FK u tabelama STANICE, OSUMNJICENI i nbp.NBP_USER.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adresa {
    /** Primarni ključ — kolona ADRESA_ID. */
    private Long adresaId;

    /** Naziv ulice i kućni broj — kolona ULICA_I_BROJ. */
    private String ulicaIBroj;

    /** Naziv grada — kolona GRAD. */
    private String grad;

    /** Poštanski broj — kolona POSTANSKI_BROJ. */
    private String postanskiBroj;

    /** Naziv države — kolona DRZAVA. */
    private String drzava;
}