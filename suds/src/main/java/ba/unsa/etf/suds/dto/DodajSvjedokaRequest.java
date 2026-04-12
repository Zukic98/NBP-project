package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DodajSvjedokaRequest {
    private String imePrezime;
    private String jmbg;
    private String kontaktTelefon;
    private String biljeska;

    /** Jednopoljna adresa iz frontenda (koristi se ako ulicaIBroj nije postavljen) */
    private String adresa;

    private String ulicaIBroj;
    private String grad;
    private String postanskiBroj;
    private String drzava;
}
