package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimNaSlucaju {
    private Long id;              // TIM_NA_SLUCAJU_ID
    private Long slucajId;        // SLUCAJ_ID
    private Long userId;          // USER_ID
    private String ulogaNaSlucaju; // ULOGA_NA_SLUCAJU
    private Timestamp datumDodavanja; // DATUM_DODAVANJA
}
