package ba.unsa.etf.suds.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class DodajOsumnjicenogRequest {
    private String imePrezime;
    private String jmbg;
    private Date datumRodjenja;

    private String ulicaIBroj;
    private String grad;
    private String postanskiBroj;
    private String drzava;
}
