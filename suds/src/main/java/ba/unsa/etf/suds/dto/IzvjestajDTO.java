package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class IzvjestajDTO {
    private SlucajInfo slucaj;
    private List<DokazInfo> dokazi;
    private List<LanacInfo> lanacNadzora;
    private List<TimInfo> tim;
    private List<SvjedokInfo> svjedoci;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SlucajInfo {
        private String brojSlucaja;
        private String status;
        private String opis;
        private String voditeljSlucaja;
        private Timestamp datumKreiranja;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DokazInfo {
        private String opis;
        private String tipDokaza;
        private String lokacijaPronalaska;
        private String prikupioIme;
        private Timestamp datumPrikupa;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LanacInfo {
        private String dokazOpis;
        private String predaoIme;
        private String preuzeoIme;
        private Timestamp datumPrimopredaje;
        private String potvrdaStatus;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class TimInfo {
        private String imePrezime;
        private String nazivUloge;
        private String ulogaNaSlucaju;
        private String email;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SvjedokInfo {
        private String imePrezime;
        private String jmbg;
        private String adresa;
        private String kontaktTelefon;
        private String biljeska;
    }
}
