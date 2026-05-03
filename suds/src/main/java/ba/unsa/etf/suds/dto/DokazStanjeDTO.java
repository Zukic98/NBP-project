package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/dokazi/{id}/stanje.
 *
 * <p>Prikazuje trenutno stanje dokaza u lancu nadzora: ko ga drži i
 * može li ga predati. JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DokazStanjeDTO {
    /** Indikator može li trenutni nosilac predati dokaz drugom uposleniku. */
    private boolean mozePredati;

    /** Informacije o trenutnom nosiocu dokaza. */
    private TrenutniNosilacInfo trenutniNosilac;

    /**
     * Detalji o uposleniku koji trenutno drži dokaz.
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TrenutniNosilacInfo {
        /** ID uposlenika koji trenutno drži dokaz. */
        private Long trenutniNosilacId;

        /** Ime i prezime uposlenika koji trenutno drži dokaz. */
        private String trenutniNosilacIme;

        /** Status posljednje primopredaje (npr. "POTVRĐENO", "NA_ČEKANJU"). */
        private String status;

        /** Datum i vrijeme posljednje primopredaje. */
        private Timestamp zadnjaPrimopredaja;
    }
}
