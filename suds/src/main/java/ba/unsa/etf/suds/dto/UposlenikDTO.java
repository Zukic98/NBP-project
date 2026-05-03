package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Odgovor za GET /api/uposlenici i GET /api/uposlenici/{id}.
 *
 * <p>Podaci o uposleniku s prilagođenim JSON imenima polja putem
 * {@code @JsonProperty} anotacija. Metoda {@link #getUloga()} mapira
 * interne nazive uloga (npr. "SEF_STANICE") u čitljive nazive za SPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UposlenikDTO {
    /** Primarni ključ korisnika iz tabele NBP_USER. */
    private Long userId;

    /** Ime uposlenika. */
    private String ime;

    /** Prezime uposlenika. */
    private String prezime;

    /** Email adresa uposlenika. */
    private String email;

    /** Korisničko ime uposlenika. */
    private String username;

    /** Naziv uloge iz tabele NBP_ROLE (interna vrijednost, npr. "SEF_STANICE"). */
    private String nazivUloge;

    /** Broj značke uposlenika iz tabele UPOSLENIK_PROFIL. */
    private String brojZnacke;

    /** Naziv policijske stanice kojoj uposlenik pripada. */
    private String nazivStanice;

    /** Radni status uposlenika (npr. "Aktivan", "Penzionisan"). */
    private String status;

    @JsonProperty("uposlenik_id")
    public Long getUposlenikId() {
        return userId;
    }

    @JsonProperty("ime_prezime")
    public String getImePrezime() {
        String i = ime != null ? ime : "";
        String p = prezime != null ? prezime : "";
        return (i + " " + p).trim();
    }

    // SEF_STANICE → Administrator, INSPEKTOR → Inspektor, FORENZIČAR → Forenzičar, POLICAJAC → Policajac
    @JsonProperty("uloga")
    public String getUloga() {
        if (nazivUloge == null) return null;
        return switch (nazivUloge) {
            case "SEF_STANICE" -> "Administrator";
            case "INSPEKTOR" -> "Inspektor";
            case "FORENZIČAR", "FORENZICAR" -> "Forenzičar";
            case "POLICAJAC" -> "Policajac";
            default -> nazivUloge;
        };
    }

    @JsonProperty("broj_znacke")
    public String getBrojZnackeSnake() {
        return brojZnacke;
    }

    @JsonProperty("naziv_uloge")
    public String getNazivUlogeSnake() {
        return nazivUloge;
    }

    @JsonProperty("naziv_stanice")
    public String getNazivStaniceSnake() {
        return nazivStanice;
    }
}