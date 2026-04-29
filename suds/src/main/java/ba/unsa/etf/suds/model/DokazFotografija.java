package ba.unsa.etf.suds.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class DokazFotografija {
    // Getteri i setteri
    private Long fotografijaId;
    private Long dokazId;
    private byte[] fotografija;
    private String nazivFajla;
    private String mimeType;
    private Long velicinaFajla;
    private Integer redniBroj;
    private Timestamp datumDodavanja;
    private Long dodaoUserId;
    private String opisFotografije;

    // Konstruktori
    public DokazFotografija() {}

    public DokazFotografija(Long fotografijaId, Long dokazId, byte[] fotografija,
                            String nazivFajla, String mimeType, Long velicinaFajla,
                            Integer redniBroj, Timestamp datumDodavanja,
                            Long dodaoUserId, String opisFotografije) {
        this.fotografijaId = fotografijaId;
        this.dokazId = dokazId;
        this.fotografija = fotografija;
        this.nazivFajla = nazivFajla;
        this.mimeType = mimeType;
        this.velicinaFajla = velicinaFajla;
        this.redniBroj = redniBroj;
        this.datumDodavanja = datumDodavanja;
        this.dodaoUserId = dodaoUserId;
        this.opisFotografije = opisFotografije;
    }

}