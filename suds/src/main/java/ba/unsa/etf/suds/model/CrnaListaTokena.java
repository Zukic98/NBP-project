package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli nbp.CRNA_LISTA_TOKENA — JWT tokeni
 * koji su eksplicitno poništeni (odjava korisnika) i ne smiju se više prihvatiti,
 * čak i ako im rok važenja još nije istekao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrnaListaTokena {
    /** Primarni ključ — kolona ID. */
    private Long id;

    /** Cijeli JWT string koji je stavljen na crnu listu — kolona TOKEN. */
    private String token;

    /** Datum i vrijeme kada token ističe; nakon toga se red može obrisati — kolona EXPIRES_AT. */
    private Timestamp expiresAt;
}