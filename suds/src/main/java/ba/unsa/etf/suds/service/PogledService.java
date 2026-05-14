package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.PogledLanacNadzoraPregledDTO;
import ba.unsa.etf.suds.dto.PogledSlucajPregledDTO;
import ba.unsa.etf.suds.dto.PogledTimNaSlucajuPregledDTO;
import ba.unsa.etf.suds.repository.PogledRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za čitanje iz Oracle pogleda (POGLED_*) — tanak omot oko
 * {@link PogledRepository} koji izlaže read-only podatke kontroleru.
 */
@Service
public class PogledService {

    private final PogledRepository pogledRepository;

    public PogledService(PogledRepository pogledRepository) {
        this.pogledRepository = pogledRepository;
    }

    public List<PogledSlucajPregledDTO> getPreglediSlucajeva() {
        return pogledRepository.findSviPreglediSlucajeva();
    }

    public List<PogledLanacNadzoraPregledDTO> getPreglediLancaNadzora() {
        return pogledRepository.findSviPreglediLancaNadzora();
    }

    public List<PogledTimNaSlucajuPregledDTO> getPreglediTimaNaSlucaju() {
        return pogledRepository.findSviPreglediTimaNaSlucaju();
    }
}
