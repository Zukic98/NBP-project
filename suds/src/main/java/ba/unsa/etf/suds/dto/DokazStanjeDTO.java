package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DokazStanjeDTO {
    private boolean mozePredati;
    private TrenutniNosilacInfo trenutniNosilac;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TrenutniNosilacInfo {
        private Long trenutniNosilacId;
        private String trenutniNosilacIme;
        private String status;
        private Timestamp zadnjaPrimopredaja;
    }
}
