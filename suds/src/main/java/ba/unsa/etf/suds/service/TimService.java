package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajClanaTRequest;
import ba.unsa.etf.suds.dto.TimClanDTO;
import ba.unsa.etf.suds.model.TimNaSlucaju;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class TimService {

    private final TimNaSlucajuRepository timNaSlucajuRepository;

    public TimService(TimNaSlucajuRepository timNaSlucajuRepository) {
        this.timNaSlucajuRepository = timNaSlucajuRepository;
    }

    public List<TimClanDTO> getClanoviTima(Long caseId) {
        return timNaSlucajuRepository.findByCaseId(caseId);
    }

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

    public void ukloniClanaTima(Long dodjelaId) {
        timNaSlucajuRepository.deleteById(dodjelaId);
    }
}
