package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli FORENZICKI_IZVJESTAJI — forenzički
 * izvještaj koji FORENZIČAR kreira za određeni dokaz.
 *
 * <p>Veže se na: DOKAZI (kroz DOKAZ_ID), nbp.NBP_USER (kroz KREATOR_USER_ID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForenzickiIzvjestaj {
    /** Primarni ključ — kolona IZVJESTAJ_ID. */
    private Long izvjestajId;

    /** FK na DOKAZI.DOKAZ_ID — dokaz na koji se izvještaj odnosi — kolona DOKAZ_ID. */
    private Long dokazId;

    /** FK na nbp.NBP_USER.ID — forenzičar koji je kreirao izvještaj — kolona KREATOR_USER_ID. */
    private Long kreatorUserId;

    /** Detaljan tekstualni sadržaj izvještaja — kolona SADRZAJ. */
    private String sadrzaj;

    /** Sažeti zaključak forenzičke analize — kolona ZAKLJUCAK. */
    private String zakljucak;

    /** Datum i vrijeme kreiranja izvještaja — kolona DATUM_KREIRANJA. */
    private Timestamp datumKreiranja;
}