package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UposlenikDTO {
    private Long userId;
    private String ime;
    private String prezime;
    private String email;
    private String username;
    private String nazivUloge;     // Iz NBP_ROLE (DB vrijednost, npr. "SEF_STANICE")
    private String brojZnacke;     // Iz UPOSLENIK_PROFIL
    private String nazivStanice;   // Iz STANICE
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