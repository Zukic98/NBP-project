package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UposlenikProfil {
    private Long profilId;
    private Long userId;
    private Long stanicaId;
    private String brojZnacke;
    private String status;
}