package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajClanaTRequest;
import ba.unsa.etf.suds.dto.TimClanDTO;
import ba.unsa.etf.suds.model.TimNaSlucaju;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * Servis za upravljanje timovima na krivičnim slučajevima.
 *
 * <p>Orkestrira {@link TimNaSlucajuRepository} kako bi podržao dodavanje,
 * uklanjanje i pregled članova tima. Polje {@code ulogaNaSlucaju} je slobodan
 * tekstualni opis uloge na konkretnom slučaju (npr. "Vođa istrage") i ne
 * odgovara sistemskoj ulozi korisnika ({@code INSPEKTOR}, {@code POLICAJAC} itd.).
 */
@Service
public class TimService {

    private final TimNaSlucajuRepository timNaSlucajuRepository;

    /** Konstruktorska injekcija repozitorija tima na slučaju. */
    public TimService(TimNaSlucajuRepository timNaSlucajuRepository) {
        this.timNaSlucajuRepository = timNaSlucajuRepository;
    }

    /**
     * Dohvata listu članova tima za dati slučaj.
     *
     * @param caseId identifikator slučaja
     * @return lista {@link TimClanDTO} objekata s podacima o svakom članu tima
     */
    public List<TimClanDTO> getClanoviTima(Long caseId) {
        return timNaSlucajuRepository.findByCaseId(caseId);
    }

    /**
     * Dodaje novog člana u tim na slučaju.
     *
     * <p>Nakon pohrane, metoda ponovo dohvata tim kako bi vratila obogaćeni
     * {@link TimClanDTO} s podacima o uposleniku (ime, uloga, značka itd.).
     *
     * @param caseId  identifikator slučaja
     * @param request podaci o uposleniku i njegovoj ulozi na slučaju
     * @return {@link TimClanDTO} s kompletnim podacima o novododanom članu
     * @throws RuntimeException ako novokreirani zapis nije pronađen nakon pohrane
     */
    public TimClanDTO dodajClanaTima(Long caseId, DodajClanaTRequest request) {
        TimNaSlucaju tim = new TimNaSlucaju();
        tim.setSlucajId(caseId);
        tim.setUserId(request.getUposlenikId());
        tim.setUlogaNaSlucaju(request.getUlogaNaSlucaju());
        tim.setDatumDodavanja(new Timestamp(System.currentTimeMillis()));

        Long dodjelaId = timNaSlucajuRepository.save(tim);

        return timNaSlucajuRepository.findByCaseId(caseId).stream()
                .filter(clan -> dodjelaId.equals(clan.getDodjelaId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error while fetching newly added team member"));
    }

    /**
     * Uklanja člana tima prema ID-u dodjele.
     *
     * <p>Napomena: parametar je {@code dodjelaId} (ID zapisa u tabeli
     * {@code TIM_NA_SLUCAJU}), a ne ID uposlenika.
     *
     * @param dodjelaId identifikator zapisa dodjele u timu
     */
    public void ukloniClanaTima(Long dodjelaId) {
        timNaSlucajuRepository.deleteById(dodjelaId);
    }
}
