package ba.unsa.etf.suds.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class DokazFotografijaDTO {
    // Getteri i setteri
    private Long fotografijaId;
    private Long dokazId;
    private String fotografijaBase64; // Base64 enkodirana slika za frontend
    private String nazivFajla;
    private String mimeType;
    private Long velicinaFajla;
    private Integer redniBroj;
    private Timestamp datumDodavanja;
    private String dodaoIme;
    private String opisFotografije;

    // Konstruktori
    public DokazFotografijaDTO() {}

}