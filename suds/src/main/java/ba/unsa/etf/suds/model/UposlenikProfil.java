package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u tabeli UPOSLENIK_PROFIL — prošireni profil
 * uposlenika koji sadrži podatke specifične za policijsku stanicu.
 *
 * <p>Veže se na: nbp.NBP_USER (kroz USER_ID), STANICE (kroz STANICA_ID).
 * Svaki NBP_USER koji je aktivan uposlenik ima tačno jedan UPOSLENIK_PROFIL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UposlenikProfil {
    /** Primarni ključ — kolona PROFIL_ID. */
    private Long profilId;

    /** FK na nbp.NBP_USER.ID — korisnički nalog uposlenika — kolona USER_ID. */
    private Long userId;

    /** FK na STANICE.STANICA_ID — stanica kojoj uposlenik pripada — kolona STANICA_ID. */
    private Long stanicaId;

    /** Jedinstveni broj policijske značke uposlenika — kolona BROJ_ZNACKE. */
    private String brojZnacke;

    /** Status uposlenika; podrazumijevana vrijednost je "Aktivan" — kolona STATUS. */
    private String status; // Po ERD-u ima default: 'Aktivan'
}