package ba.unsa.etf.suds.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/dokazi/{id}/fotografije.
 *
 * <p>Predstavlja jednu fotografiju dokaza s Base64-enkodiranim sadržajem
 * pogodnim za direktno prikazivanje u SPA-u bez dodatnih zahtjeva.
 */
@Setter
@Getter
public class DokazFotografijaDTO {
    /** Primarni ključ fotografije iz tabele FOTOGRAFIJE_DOKAZA. */
    private Long fotografijaId;

    /** ID dokaza kojemu fotografija pripada. */
    private Long dokazId;

    /** Base64-enkodirana slika za direktno prikazivanje u frontendu. */
    private String fotografijaBase64;

    /** Originalni naziv fajla fotografije. */
    private String nazivFajla;

    /** MIME tip fotografije (npr. "image/jpeg"). */
    private String mimeType;

    /** Veličina fajla u bajtovima. */
    private Long velicinaFajla;

    /** Redni broj fotografije unutar skupa fotografija dokaza. */
    private Integer redniBroj;

    /** Datum i vrijeme dodavanja fotografije. */
    private Timestamp datumDodavanja;

    /** Ime i prezime uposlenika koji je dodao fotografiju. */
    private String dodaoIme;

    /** Tekstualni opis sadržaja fotografije. */
    private String opisFotografije;

    // Konstruktori
    public DokazFotografijaDTO() {}

}