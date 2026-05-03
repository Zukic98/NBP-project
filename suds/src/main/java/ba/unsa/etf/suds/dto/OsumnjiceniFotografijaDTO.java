package ba.unsa.etf.suds.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/osumnjiceni/{id}/fotografije.
 *
 * <p>Predstavlja jednu fotografiju osumnjičenog s Base64-enkodiranim sadržajem
 * pogodnim za direktno prikazivanje u SPA-u. Uključuje podatke o izmjenama
 * radi revizijskog traga.
 */
@Setter
@Getter
public class OsumnjiceniFotografijaDTO {
    /** Primarni ključ fotografije iz tabele FOTOGRAFIJE_OSUMNJICENIH. */
    private Long fotografijaId;

    /** ID osumnjičenog kojemu fotografija pripada. */
    private Long osumnjiceniId;

    /** Base64-enkodirana slika za direktno prikazivanje u frontendu. */
    private String fotografijaBase64;

    /** Originalni naziv fajla fotografije. */
    private String nazivFajla;

    /** MIME tip fotografije (npr. "image/jpeg"). */
    private String mimeType;

    /** Veličina fajla u bajtovima. */
    private Long velicinaFajla;

    /** Redni broj fotografije unutar skupa fotografija osumnjičenog. */
    private Integer redniBroj;

    /** Datum i vrijeme dodavanja fotografije. */
    private Timestamp datumDodavanja;

    /** Ime i prezime uposlenika koji je dodao fotografiju. */
    private String dodaoIme;

    /** Datum i vrijeme posljednje izmjene fotografije. */
    private Timestamp datumIzmjene;

    /** Ime i prezime uposlenika koji je posljednji izmijenio fotografiju. */
    private String izmijenioIme;

    /** Tekstualni opis sadržaja fotografije. */
    private String opisFotografije;

    /** Status fotografije (npr. "AKTIVAN", "ARHIVIRAN"). */
    private String status;

    // Konstruktori
    public OsumnjiceniFotografijaDTO() {}

}