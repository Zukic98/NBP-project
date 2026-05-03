package ba.unsa.etf.suds.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli OSUMNJICENI_FOTOGRAFIJE — fotografija
 * (mugshot ili druga slika) priložena uz profil osumnjičenog.
 *
 * <p>Veže se na: OSUMNJICENI (kroz OSUMNJICENI_ID), nbp.NBP_USER (kroz
 * DODAO_USER_ID i IZMIJENIO_USER_ID). Polje {@code fotografija} je BLOB kolona;
 * preporučena maksimalna veličina fajla je 10 MB (ograničenje na nivou servisa).
 */
@Setter
@Getter
public class OsumnjiceniFotografija {
    /** Primarni ključ — kolona FOTOGRAFIJA_ID. */
    private Long fotografijaId;

    /** FK na OSUMNJICENI.OSUMNJICENI_ID — osumnjičeni kome fotografija pripada — kolona OSUMNJICENI_ID. */
    private Long osumnjiceniId;

    /** Binarni sadržaj fotografije (BLOB) — kolona FOTOGRAFIJA. */
    private byte[] fotografija;

    /** Originalni naziv fajla pri uploadu — kolona NAZIV_FAJLA. */
    private String nazivFajla;

    /** MIME tip fajla (npr. "image/jpeg", "image/png") — kolona MIME_TYPE. */
    private String mimeType;

    /** Veličina fajla u bajtovima — kolona VELICINA_FAJLA. */
    private Long velicinaFajla;

    /** Redni broj fotografije unutar skupa fotografija jednog osumnjičenog — kolona REDNI_BROJ. */
    private Integer redniBroj;

    /** Datum i vrijeme dodavanja fotografije — kolona DATUM_DODAVANJA. */
    private Timestamp datumDodavanja;

    /** FK na nbp.NBP_USER.ID — korisnik koji je dodao fotografiju — kolona DODAO_USER_ID. */
    private Long dodaoUserId;

    /** Datum i vrijeme posljednje izmjene fotografije — kolona DATUM_IZMJENE. */
    private Timestamp datumIzmjene;

    /** FK na nbp.NBP_USER.ID — korisnik koji je posljednji izmijenio fotografiju — kolona IZMIJENIO_USER_ID. */
    private Long izmijenioUserId;

    /** Slobodni tekstualni opis sadržaja fotografije — kolona OPIS_FOTOGRAFIJE. */
    private String opisFotografije;

    /** Status fotografije (npr. "Aktivan", "Arhiviran") — kolona STATUS. */
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