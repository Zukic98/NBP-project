package ba.unsa.etf.suds.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class OsumnjiceniFotografija {
    // Getteri i setteri
    private Long fotografijaId;
    private Long osumnjiceniId;
    private byte[] fotografija;
    private String nazivFajla;
    private String mimeType;
    private Long velicinaFajla;
    private Integer redniBroj;
    private Timestamp datumDodavanja;
    private Long dodaoUserId;
    private Timestamp datumIzmjene;
    private Long izmijenioUserId;
    private String opisFotografije;
    private String status;

    // Konstruktori
    public OsumnjiceniFotografija() {}

    public OsumnjiceniFotografija(Long fotografijaId, Long osumnjiceniId, byte[] fotografija,
                                  String nazivFajla, String mimeType, Long velicinaFajla,
                                  Integer redniBroj, Timestamp datumDodavanja, Long dodaoUserId,
                                  Timestamp datumIzmjene, Long izmijenioUserId,
                                  String opisFotografije, String status) {
        this.fotografijaId = fotografijaId;
        this.osumnjiceniId = osumnjiceniId;
        this.fotografija = fotografija;
        this.nazivFajla = nazivFajla;
        this.mimeType = mimeType;
        this.velicinaFajla = velicinaFajla;
        this.redniBroj = redniBroj;
        this.datumDodavanja = datumDodavanja;
        this.dodaoUserId = dodaoUserId;
        this.datumIzmjene = datumIzmjene;
        this.izmijenioUserId = izmijenioUserId;
        this.opisFotografije = opisFotografije;
        this.status = status;
    }

}