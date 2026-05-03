package ba.unsa.etf.suds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Odgovor servera na POST /api/auth/login.
 *
 * <p>Sadrži JWT token koji klijent treba pohraniti i slati u
 * {@code Authorization: Bearer <token>} zaglavlju svakog zahtjeva.
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    /** JWT token koji identificira prijavljenog uposlenika. */
    private String token;

    /** Tip tokena — uvijek {@code "Bearer"}. */
    private String type = "Bearer";
}
