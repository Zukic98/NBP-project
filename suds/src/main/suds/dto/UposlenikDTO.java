package ba.unsa.etf.suds.ba.unsa.etf.suds.dto;

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
    private String nazivUloge;     // Iz NBP_ROLE
    private String brojZnacke;     // Iz UPOSLENIK_PROFIL
    private String nazivStanice;   // Iz STANICE
}