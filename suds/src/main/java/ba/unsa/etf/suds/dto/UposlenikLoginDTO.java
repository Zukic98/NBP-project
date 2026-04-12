package ba.unsa.etf.suds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UposlenikLoginDTO {
    private Long userId;
    private String ime;
    private String prezime;
    private String email;
    private String password;
    private String uloga;
    private String brojZnacke;
    private String status;
    private Long stanicaId;
}