package ba.unsa.etf.suds.dto;

import lombok.Data;

@Data
public class DodajSvjedokaRequest {
    private String imePrezime;
    private String jmbg;
    private String kontaktTelefon;
    private String biljeska;

    private String ulicaIBroj;
    private String grad;
    private String postanskiBroj;
    private String drzava;
}
