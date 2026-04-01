package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.SlucajDetaljiDTO;
import ba.unsa.etf.suds.repository.SlucajRepository;
import org.springframework.stereotype.Service;

@Service
public class SlucajService {
    private final SlucajRepository slucajRepository;

    public SlucajService(SlucajRepository slucajRepository) {
        this.slucajRepository = slucajRepository;
    }

    /**
     * Metoda dohvaća sve detalje o slučaju na osnovu broja slučaja.
     * Koristi jedan kompleksan JOIN upit definisan u repozitoriju.
     */
    public SlucajDetaljiDTO getSlucajDetalji(String brojSlucaja) {
        return slucajRepository.findDetaljiByBroj(brojSlucaja);
    }
}