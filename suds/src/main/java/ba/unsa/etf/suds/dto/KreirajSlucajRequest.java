package ba.unsa.etf.suds.dto;

import lombok.Data;

import java.util.List;

/**
 * Tijelo zahtjeva za POST /api/slucajevi.
 *
 * <p>Kreira novi krivični slučaj s adresom mjesta događaja i inicijalnim
 * sastavom tima. Svaki član tima navodi se kao {@link ClanTima} s ulogom
 * na slučaju.
 */
@Data
public class KreirajSlucajRequest {
    /** Jedinstveni broj slučaja (npr. "KT-2024-001"). */
    private String brojSlucaja;

    /** Tekstualni opis slučaja. */
    private String opis;

    /** ID stanice koja vodi slučaj. */
    private Long stanicaId;

    /** Ulica i kućni broj mjesta događaja. */
    private String ulicaIBroj;

    /** Grad mjesta događaja. */
    private String grad;

    /** Poštanski broj mjesta događaja. */
    private String postanskiBroj;

    /** Država mjesta događaja. */
    private String drzava;

    /** Inicijalni sastav tima dodjeljenog slučaju. */
    private List<ClanTima> tim;

    /**
     * Jedan član tima koji se dodjeljuje slučaju pri kreiranju.
     */
    @Data
    public static class ClanTima {
        /** ID korisnika (NBP_USER) koji se dodaje u tim. */
        private Long userId;

        /** Uloga člana na ovom slučaju (npr. "VODITELJ", "ISTRAŽITELJ"). */
        private String uloga;
    }
}
