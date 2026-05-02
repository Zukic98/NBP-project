package ba.unsa.etf.suds.dto;

import lombok.Data;

/**
 * Tijelo zahtjeva za PATCH /api/uposlenici/{id}/status.
 *
 * <p>Šef stanice mijenja radni status uposlenika.
 */
@Data
public class PromijeniStatusRequest {
    /** Novi status uposlenika: "Aktivan", "Penzionisan" ili "Otpušten". */
    private String status;
}