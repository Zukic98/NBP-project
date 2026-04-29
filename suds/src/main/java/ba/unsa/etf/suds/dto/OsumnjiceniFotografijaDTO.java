package ba.unsa.etf.suds.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class OsumnjiceniFotografijaDTO {
    // Getteri i setteri
    private Long fotografijaId;
    private Long osumnjiceniId;
    private String fotografijaBase64;
    private String nazivFajla;
    private String mimeType;
    private Long velicinaFajla;
    private Integer redniBroj;
    private Timestamp datumDodavanja;
    private String dodaoIme;
    private Timestamp datumIzmjene;
    private String izmijenioIme;
    private String opisFotografije;
    private String status;

    // Konstruktori
    public OsumnjiceniFotografijaDTO() {}

}