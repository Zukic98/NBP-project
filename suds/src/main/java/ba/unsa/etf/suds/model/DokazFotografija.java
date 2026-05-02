package ba.unsa.etf.suds.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli DOKAZ_FOTOGRAFIJE — fotografija
 * priložena uz određeni dokaz u krivičnom predmetu.
 *
 * <p>Veže se na: DOKAZI (kroz DOKAZ_ID), nbp.NBP_USER (kroz DODAO_USER_ID).
 * Polje {@code fotografija} je BLOB kolona; preporučena maksimalna veličina
 * fajla je 10 MB (ograničenje na nivou servisa).
 */
@Setter
@Getter
public class DokazFotografija {
    /** Primarni ključ — kolona FOTOGRAFIJA_ID. */
    private Long fotografijaId;

    /** FK na DOKAZI.DOKAZ_ID — dokaz kome fotografija pripada — kolona DOKAZ_ID. */
    private Long dokazId;

    /** Binarni sadržaj fotografije (BLOB) — kolona FOTOGRAFIJA. */
    private byte[] fotografija;

    /** Originalni naziv fajla pri uploadu — kolona NAZIV_FAJLA. */
    private String nazivFajla;

    /** MIME tip fajla (npr. "image/jpeg", "image/png") — kolona MIME_TYPE. */
    private String mimeType;

    /** Veličina fajla u bajtovima — kolona VELICINA_FAJLA. */
    private Long velicinaFajla;

    /** Redni broj fotografije unutar skupa fotografija jednog dokaza — kolona REDNI_BROJ. */
    private Integer redniBroj;

    /** Datum i vrijeme dodavanja fotografije — kolona DATUM_DODAVANJA. */
    private Timestamp datumDodavanja;

    /** FK na nbp.NBP_USER.ID — korisnik koji je dodao fotografiju — kolona DODAO_USER_ID. */
    private Long dodaoUserId;

    /** Slobodni tekstualni opis sadržaja fotografije — kolona OPIS_FOTOGRAFIJE. */
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